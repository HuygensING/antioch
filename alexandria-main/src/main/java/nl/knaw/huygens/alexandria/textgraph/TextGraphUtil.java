package nl.knaw.huygens.alexandria.textgraph;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import nl.knaw.huygens.alexandria.api.model.BaseElementDefinition;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;
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
    appendClosingElements(builder, i.get(), xmlAnnotationList);
    return builder.toString();
  }

  private static void appendOpeningElements(StringBuilder builder, int i, List<XmlAnnotation> xmlAnnotations) {
    Collection<Integer> elementsToOpenIndexes = openBeforeText.get(i);
    elementsToOpenIndexes.forEach(j -> {
      XmlAnnotation xmlAnnotation = xmlAnnotations.get(j);
      builder.append("<").append(xmlAnnotation.getName());
      for (Map.Entry<String, String> entry : xmlAnnotation.getAttributes().entrySet()) {
        builder.append(' ').append(entry.getKey()).append('=');
        builder.append('"');
        appendAttributeValue(builder, entry.getValue());
        builder.append('"');
      }
      if (xmlAnnotation.isMilestone()) {
        builder.append("/");
      }
      builder.append(">");
    });
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
}
