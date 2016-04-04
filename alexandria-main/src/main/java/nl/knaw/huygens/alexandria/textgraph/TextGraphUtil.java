package nl.knaw.huygens.alexandria.textgraph;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.StreamingOutput;

import org.jooq.lambda.Unchecked;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import nl.knaw.huygens.alexandria.api.model.BaseElementDefinition;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.tei.Document;

public class TextGraphUtil {

  private static Multimap<Integer, Integer> openBeforeText = ArrayListMultimap.create();
  private static Multimap<Integer, Integer> closeAfterText = ArrayListMultimap.create();

  public static ParseResult parse(String xml) {
    ParseResult result = new ParseResult();
    Document document = Document.createFromXml(xml, true);
    XmlVisitor visitor = new XmlVisitor(result);
    document.accept(visitor);
    return result;
  }

  public static String renderBaseLayer(List<String> textSegments, Set<XmlAnnotation> xmlAnnotations, BaseLayerDefinition baselayerDefinition) {
    List<String> baseElementNames = baselayerDefinition.getBaseElementDefinitions().stream().map(BaseElementDefinition::getName).collect(toList());
    StringBuilder builder = new StringBuilder();

    List<XmlAnnotation> xmlAnnotationList = Lists.newArrayList(xmlAnnotations);
    for (int i = 0; i < xmlAnnotationList.size(); i++) {
      XmlAnnotation xmlAnnotation = xmlAnnotationList.get(i);
      if (baseElementNames.contains(xmlAnnotation.getName())) {
        openBeforeText.put(xmlAnnotation.getFirstSegmentIndex(), i);
        closeAfterText.put(xmlAnnotation.getLastSegmentIndex(), i);
      }
    }

    final AtomicInteger i = new AtomicInteger(0);
    textSegments.forEach(t -> {
      appendOpeningElements(builder, i.get(), xmlAnnotationList);
      builder.append(t);
      appendClosingElements(builder, i.getAndIncrement(), xmlAnnotationList);
    });
    return builder.toString();
  }

  private static void appendOpeningElements(StringBuilder builder, int i, List<XmlAnnotation> xmlAnnotations) {
    Collection<Integer> elementsToOpenIndexes = openBeforeText.get(i);
    elementsToOpenIndexes.forEach(j -> {
      XmlAnnotation xmlAnnotation = xmlAnnotations.get(j);
      builder.append("<").append(xmlAnnotation.getName());
      Map<String, String> attributes = xmlAnnotation.getAttributes();
      appendAttributes(builder, attributes);
      if (xmlAnnotation.isMilestone()) {
        builder.append("/");
      }
      builder.append(">");
    });
  }

  public static void appendAttributes(StringBuilder builder, Map<String, String> attributes) {
    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      builder.append(' ').append(entry.getKey()).append('=');
      builder.append('"');
      appendAttributeValue(builder, entry.getValue());
      builder.append('"');
    }
  }

  private static void appendClosingElements(StringBuilder builder, int i, List<XmlAnnotation> xmlAnnotations) {
    List<Integer> elementsToCloseIndexes = Lists.reverse(Lists.newArrayList(closeAfterText.get(i)));
    elementsToCloseIndexes.forEach(j -> {
      XmlAnnotation xmlAnnotation = xmlAnnotations.get(j);
      if (!xmlAnnotation.isMilestone()) {
        builder.append("</")//
            .append(xmlAnnotation.getName())//
            .append(">");
      }
    });
  }

  private static void appendAttributeValue(StringBuilder builder, String value) {
    int n = value.length();
    for (int i = 0; i < n; i++) {
      char c = value.charAt(i);
      switch (c) {
      case '<':
        builder.append("&lt;");
        break;
      case '>':
        builder.append("&gt;");
        break;
      case '&':
        builder.append("&amp;");
        break;
      default:
        builder.append(c);
        break;
      }
    }
  }

  public static String getMilestoneTag(String name, Map<String, String> attributes) {
    return openingTagBuilder(name, attributes).append("/>").toString();
  }

  public static String getOpenTag(String name, Map<String, String> attributes) {
    return openingTagBuilder(name, attributes).append(">").toString();
  }

  public static String getCloseTag(String name) {
    return "</" + name + ">";
  }

  private static StringBuilder openingTagBuilder(String name, Map<String, String> attributes) {
    StringBuilder builder = new StringBuilder("<").append(name);
    appendAttributes(builder, attributes);
    return builder;
  }

  public static StreamingOutput streamBaseLayerXML(AlexandriaService service, UUID resourceId, BaseLayerDefinition baseLayerDefinition) {
    List<BaseElementDefinition> baseElementDefinitions = baseLayerDefinition.getBaseElementDefinitions();
    StreamingOutput outputstream = output -> {
      service.runInTransaction(Unchecked.runnable(() -> {
        Writer writer = new BufferedWriter(new OutputStreamWriter(output));
        service.getTextGraphSegmentStream(resourceId).forEach(Unchecked.consumer(//
            segment -> TextGraphUtil.streamTextGraphSegment(writer, segment, baseElementDefinitions)//
        ));
        writer.flush();
      }));
    };
    return outputstream;
  }

  public static void streamTextGraphSegment(Writer writer, TextGraphSegment segment, List<BaseElementDefinition> baseElementDefinitions) throws IOException {
    Set<String> baseElementNames = baseElementDefinitions.stream().map(BaseElementDefinition::getName).collect(toSet());
    Map<String, List<String>> baseElementAttributes = Maps.newHashMap();
    for (BaseElementDefinition bed : baseElementDefinitions) {
      baseElementAttributes.put(bed.getName(), bed.getBaseAttributes());
    }
    if (segment.isMilestone()) {
      TextAnnotation milestone = segment.getMilestone();
      String name = milestone.getName();
      if (baseElementNames.contains(name)) {
        Map<String, String> baseAttributes = baseAttributes(baseElementAttributes.get(name), milestone);
        String openTag = TextGraphUtil.getMilestoneTag(name, baseAttributes);
        writer.write(openTag);
      }

    } else {
      for (TextAnnotation textAnnotation : segment.getTextAnnotationsToOpen()) {
        String name = textAnnotation.getName();
        if (baseElementNames.contains(name)) {
          Map<String, String> baseAttributes = baseAttributes(baseElementAttributes.get(name), textAnnotation);
          String openTag = TextGraphUtil.getOpenTag(name, baseAttributes);
          writer.write(openTag);
        }
      }
      writer.write(segment.getTextSegment());
      for (TextAnnotation textAnnotation : segment.getTextAnnotationsToClose()) {
        String name = textAnnotation.getName();
        if (baseElementNames.contains(name)) {
          String closeTag = TextGraphUtil.getCloseTag(name);
          writer.write(closeTag);
        }
      }
    }
  }

  private static Map<String, String> baseAttributes(List<String> baseElementAttributeNames, TextAnnotation milestone) {
    Map<String, String> allAttributes = milestone.getAttributes();
    Map<String, String> baseAttributes = Maps.newHashMap();
    for (String name : baseElementAttributeNames) {
      if (allAttributes.containsKey(name)) {
        baseAttributes.put(name, allAttributes.get(name));
      }
    }
    return baseAttributes;
  }

}
