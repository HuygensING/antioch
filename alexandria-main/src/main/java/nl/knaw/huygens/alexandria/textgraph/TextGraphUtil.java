package nl.knaw.huygens.alexandria.textgraph;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Consumer;

import javax.ws.rs.core.StreamingOutput;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import nl.knaw.huygens.alexandria.api.model.ElementView;
import nl.knaw.huygens.alexandria.api.model.TextView;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.tei.Document;

public class TextGraphUtil {
  private static Multimap<Integer, Integer> openBeforeText = ArrayListMultimap.create();
  private static Multimap<Integer, Integer> closeAfterText = ArrayListMultimap.create();

  public static ParseResult parse(String xml) {
    ParseResult result = new ParseResult();
    Document document = Document.createFromXml(xml, true);
    // TODO: verify xml:ids are unique
    XmlVisitor visitor = new XmlVisitor(result);
    document.accept(visitor);
    return result;
  }

  public static StreamingOutput streamXML(AlexandriaService service, UUID resourceId) {
    StreamingOutput outputstream = output -> {
      Writer writer = createBufferedUTF8OutputStreamWriter(output);
      Consumer<TextGraphSegment> action = segment -> streamTextGraphSegment(writer, segment);
      stream(service, resourceId, writer, action);
    };
    return outputstream;
  }

  public static void streamTextGraphSegment(Writer writer, TextGraphSegment segment) {
    try {
      if (segment.isMilestone()) {
        TextAnnotation milestone = segment.getMilestone();
        String name = milestone.getName();
        String openTag = getMilestoneTag(name, milestone.getAttributes());
        writer.write(openTag);

      } else {
        for (TextAnnotation textAnnotation : segment.getTextAnnotationsToOpen()) {
          String name = textAnnotation.getName();
          String openTag = getOpenTag(name, textAnnotation.getAttributes());
          writer.write(openTag);
        }
        writer.write(segment.getTextSegment());
        for (TextAnnotation textAnnotation : segment.getTextAnnotationsToClose()) {
          String name = textAnnotation.getName();
          String closeTag = getCloseTag(name);
          writer.write(closeTag);
        }
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static StreamingOutput streamTextViewXML(AlexandriaService service, UUID resourceId, TextView textView) {
    StreamingOutput outputstream = output -> {
      Writer writer = createBufferedUTF8OutputStreamWriter(output);
      TextViewContext textViewContext = new TextViewContext(textView);
      Consumer<TextGraphSegment> action = segment -> streamTextGraphSegment(writer, segment, textViewContext);
      stream(service, resourceId, writer, action);
    };
    return outputstream;
  }

  // TODO: use new TextView
  private static class TextViewContext {
    private Map<String, ElementView> elementViewMap;
    private Stack<String> ignoredAnnotationStack = new Stack<>();

    public TextViewContext(TextView textView) {
      elementViewMap = textView.getElementViewMap();
    }

    public boolean includeTag(String name) {
      if (elementViewMap.containsKey(name)){
        elementViewMap.get(name).getElementMode();
      }
//      boolean notIgnoredElement = !elementsToIgnore.contains(name);
//      boolean inclusiveAndInclude = mode.equals(Mode.inclusive) && elementNamesToInclude.contains(name);
//      boolean exclusiveAndInclude = mode.equals(Mode.exclusive) && !elementNamesToExclude.contains(name);
//      return notIgnoredElement && notInsideIgnoredElement() && (inclusiveAndInclude || exclusiveAndInclude);
      return true;
    }

    public Map<String, String> includedAttributes(TextAnnotation textAnnotation) {
      // if (mode.equals(Mode.exclusive)) {
      // return textAnnotation.getAttributes();
      // }
      // List<String> includedElementAttributeNames = elementAttributesToInclude.get(textAnnotation.getName());
      Map<String, String> allAttributes = textAnnotation.getAttributes();
      Map<String, String> attributesToInclude = Maps.newHashMap();
      // for (String name : includedElementAttributeNames) {
      // if (allAttributes.containsKey(name)) {
      // attributesToInclude.put(name, allAttributes.get(name));
      // }
      // }
      return attributesToInclude;
    }

    public void pushWhenIgnoring(TextAnnotation textAnnotation) {
      // if (elementsToIgnore.contains(textAnnotation.getName())) {
      // ignoredAnnotationStack.push(textAnnotation);
      // }
    }

    public void popWhenIgnoring(String name) {
      if (/* elementsToIgnore.contains(name) && */!ignoredAnnotationStack.isEmpty()) {
        ignoredAnnotationStack.pop();
      }
    }

    public boolean notInsideIgnoredElement() {
      return ignoredAnnotationStack.isEmpty();
    }
  }

  public static void streamTextGraphSegment(Writer writer, TextGraphSegment segment, TextViewContext textViewContext) {
    try {
      if (segment.isMilestone()) {
        TextAnnotation milestone = segment.getMilestone();
        String name = milestone.getName();
        if (textViewContext.includeTag(name)) {
          String openTag = getMilestoneTag(name, textViewContext.includedAttributes(milestone));
          writer.write(openTag);
        }

      } else {
        for (TextAnnotation textAnnotation : segment.getTextAnnotationsToOpen()) {
          String name = textAnnotation.getName();
          if (textViewContext.includeTag(name)) {
            String openTag = getOpenTag(name, textViewContext.includedAttributes(textAnnotation));
            writer.write(openTag);
          }
          textViewContext.pushWhenIgnoring(textAnnotation);
        }
        if (textViewContext.notInsideIgnoredElement()) {
          writer.write(segment.getTextSegment());
        }
        for (TextAnnotation textAnnotation : segment.getTextAnnotationsToClose()) {
          String name = textAnnotation.getName();
          if (textViewContext.includeTag(name)) {
            String closeTag = getCloseTag(name);
            writer.write(closeTag);
          }
          textViewContext.popWhenIgnoring(name);
        }
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
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

  public static void appendAttributes(StringBuilder builder, Map<String, String> attributes) {
    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      builder.append(' ').append(entry.getKey()).append('=');
      builder.append('"');
      appendAttributeValue(builder, entry.getValue());
      builder.append('"');
    }
  }

  // public static String renderTextView(List<String> textSegments, Set<XmlAnnotation> xmlAnnotations, TextView textView) {
  // List<String> includedElementNames = textView.getIncludedElementDefinitions()//
  // .stream()//
  // .map(ElementDefinition::getName)//
  // .collect(toList());
  // StringBuilder builder = new StringBuilder();
  //
  // List<XmlAnnotation> xmlAnnotationList = Lists.newArrayList(xmlAnnotations);
  // for (int i = 0; i < xmlAnnotationList.size(); i++) {
  // XmlAnnotation xmlAnnotation = xmlAnnotationList.get(i);
  // if (includedElementNames.contains(xmlAnnotation.getName())) {
  // openBeforeText.put(xmlAnnotation.getFirstSegmentIndex(), i);
  // closeAfterText.put(xmlAnnotation.getLastSegmentIndex(), i);
  // }
  // }
  //
  // final AtomicInteger i = new AtomicInteger(0);
  // textSegments.forEach(t -> {
  // appendOpeningElements(builder, i.get(), xmlAnnotationList);
  // builder.append(t);
  // appendClosingElements(builder, i.getAndIncrement(), xmlAnnotationList);
  // });
  // return builder.toString();
  // }

  /* private methods */

  private static Writer createBufferedUTF8OutputStreamWriter(OutputStream output) throws UnsupportedEncodingException {
    Writer writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
    return writer;
  }

  private static void stream(AlexandriaService service, UUID resourceId, Writer writer, Consumer<TextGraphSegment> action) throws IOException {
    service.runInTransaction(() -> {
      service.getTextGraphSegmentStream(resourceId).forEach(action);
    });
    writer.flush();
  }

  private static StringBuilder openingTagBuilder(String name, Map<String, String> attributes) {
    StringBuilder builder = new StringBuilder("<").append(name);
    appendAttributes(builder, attributes);
    return builder;
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

  // private static void appendOpeningElements(StringBuilder builder, int i, List<XmlAnnotation> xmlAnnotations) {
  // Collection<Integer> elementsToOpenIndexes = openBeforeText.get(i);
  // elementsToOpenIndexes.forEach(j -> {
  // XmlAnnotation xmlAnnotation = xmlAnnotations.get(j);
  // builder.append("<").append(xmlAnnotation.getName());
  // Map<String, String> attributes = xmlAnnotation.getAttributes();
  // appendAttributes(builder, attributes);
  // if (xmlAnnotation.isMilestone()) {
  // builder.append("/");
  // }
  // builder.append(">");
  // });
  // }
  //
  // private static void appendClosingElements(StringBuilder builder, int i, List<XmlAnnotation> xmlAnnotations) {
  // List<Integer> elementsToCloseIndexes = Lists.reverse(Lists.newArrayList(closeAfterText.get(i)));
  // elementsToCloseIndexes.forEach(j -> {
  // XmlAnnotation xmlAnnotation = xmlAnnotations.get(j);
  // if (!xmlAnnotation.isMilestone()) {
  // builder.append("</")//
  // .append(xmlAnnotation.getName())//
  // .append(">");
  // }
  // });
  // }

}
