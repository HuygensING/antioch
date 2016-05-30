package nl.knaw.huygens.alexandria.textgraph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import nl.knaw.huygens.alexandria.api.model.text.view.AttributePreCondition;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeMode;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.ElementMode;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.tei.Document;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;

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
    return output -> {
      Writer writer = createBufferedUTF8OutputStreamWriter(output);
      Consumer<TextGraphSegment> action = segment -> streamTextGraphSegment(writer, segment);
      stream(service, resourceId, writer, action);
    };
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
    return output -> {
      Writer writer = createBufferedUTF8OutputStreamWriter(output);
      TextViewContext textViewContext = new TextViewContext(textView);
      Consumer<TextGraphSegment> action = segment -> streamTextGraphSegment(writer, segment, textViewContext);
      stream(service, resourceId, writer, action);
    };
  }

  public static String asString(StreamingOutput outputStream) {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    String xml;
    try {
      outputStream.write(output);
      output.close();
      xml = output.toString();
    } catch (WebApplicationException | IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return xml;
  }

  protected static class TextViewContext {
    private Map<String, ElementView> elementViewMap;
    private Stack<TextAnnotation> ignoredAnnotationStack = new Stack<>();

    public TextViewContext(TextView textView) {
      elementViewMap = textView.getElementViewMap();
    }

    public boolean includeTag(String name, Map<String, String> attributes) {
      ElementView defaultElementView = elementViewMap.get(TextViewDefinition.DEFAULT_ATTRIBUTENAME);
      ElementView elementView = elementViewMap.getOrDefault(name, defaultElementView);
      ElementMode elementMode = elementView.getElementMode();
      ElementMode defaultViewElementMode = defaultElementView.getElementMode();
      Optional<AttributePreCondition> preCondition = elementView.getPreCondition();
      boolean includeAccordingToElementMode = elementMode.equals(ElementMode.show);
      boolean preConditionIsMet = preConditionIsMet(preCondition, attributes);
      if (!preConditionIsMet) {
        includeAccordingToElementMode = defaultViewElementMode.equals(ElementMode.show);
      }

      return notInsideIgnoredElement() && includeAccordingToElementMode;
    }

    private boolean preConditionIsMet(Optional<AttributePreCondition> preCondition, Map<String, String> attributes) {
      if (preCondition.isPresent()) {
        AttributePreCondition attributePreCondition = preCondition.get();
        String attribute = attributePreCondition.getAttribute();
        List<String> values = attributePreCondition.getValues();
        String actualValue = attributes.get(attribute);
        switch (attributePreCondition.getFunction()) {
          case is:
            return values.contains(actualValue);
          case isNot:
            return !values.contains(actualValue);
          case firstOf:
            // TODO
            break;
        }

      }
      return true;
    }

    public Map<String, String> includedAttributes(TextAnnotation textAnnotation) {
      ElementView elementView = elementViewMap.getOrDefault(textAnnotation.getName(), elementViewMap.get(TextViewDefinition.DEFAULT_ATTRIBUTENAME));
      Map<String, String> allAttributes = textAnnotation.getAttributes();
      Map<String, String> attributesToInclude = Maps.newHashMap();
      AttributeMode attributeMode = elementView.getAttributeMode();

      switch (attributeMode) {
        case showAll:
          return allAttributes;

        case hideAll:
          break;

        case showOnly:
          allAttributes.forEach((k, v) -> {
            if (elementView.getRelevantAttributes().contains(k)) {
              attributesToInclude.put(k, v);
            }
          });
          break;

        case hideOnly:
          allAttributes.forEach((k, v) -> {
            if (!elementView.getRelevantAttributes().contains(k)) {
              attributesToInclude.put(k, v);
            }
          });
          break;

        default:
          throw new RuntimeException("unexpected attributemode: " + attributeMode);
      }
      return attributesToInclude;
    }

    public void pushWhenIgnoring(TextAnnotation textAnnotation) {
      boolean ignoring = determineIgnoring(textAnnotation);
      if (ignoring) {
        ignoredAnnotationStack.push(textAnnotation);
      }
    }

    private boolean determineIgnoring(TextAnnotation textAnnotation) {
      ElementView defaultElementView = elementViewMap.get(TextViewDefinition.DEFAULT_ATTRIBUTENAME);
      ElementView elementView = elementViewMap.getOrDefault(textAnnotation.getName(), defaultElementView);
      ElementMode elementMode = elementView.getElementMode();
      ElementMode defaultElementMode = defaultElementView.getElementMode();
      Optional<AttributePreCondition> preCondition = elementView.getPreCondition();
      boolean preConditionIsMet = preConditionIsMet(preCondition, textAnnotation.getAttributes());
      boolean hideThisElement = elementMode.equals(ElementMode.hide);
      if (!preConditionIsMet) {
        hideThisElement = defaultElementMode.equals(ElementMode.hide);
      }
      return hideThisElement;
    }

    public void popWhenIgnoring(TextAnnotation textAnnotation) {
      boolean ignoring = determineIgnoring(textAnnotation);
      if (ignoring && !ignoredAnnotationStack.isEmpty()) {
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
        if (textViewContext.includeTag(name, milestone.getAttributes())) {
          String openTag = getMilestoneTag(name, textViewContext.includedAttributes(milestone));
          writer.write(openTag);
        }

      } else {
        for (TextAnnotation textAnnotation : segment.getTextAnnotationsToOpen()) {
          String name = textAnnotation.getName();
          if (textViewContext.includeTag(name, textAnnotation.getAttributes())) {
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
          if (textViewContext.includeTag(name, textAnnotation.getAttributes())) {
            String closeTag = getCloseTag(name);
            writer.write(closeTag);
          }
          textViewContext.popWhenIgnoring(textAnnotation);
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
    return new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
  }

  private static void stream(AlexandriaService service, UUID resourceId, Writer writer, Consumer<TextGraphSegment> action) throws IOException {
    service.runInTransaction(() -> service.getTextGraphSegmentStream(resourceId).forEach(action));
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
