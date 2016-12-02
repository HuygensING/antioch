package nl.knaw.huygens.alexandria.textgraph;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Consumer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import nl.knaw.huygens.alexandria.api.model.text.view.AttributePreCondition;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeMode;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.ElementMode;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.tei.Document;

public class TextGraphUtil {
  // private static Multimap<Integer, Integer> openBeforeText = ArrayListMultimap.create();
  // private static Multimap<Integer, Integer> closeAfterText = ArrayListMultimap.create();

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
      writeOpenTags(writer, segment);
      writeMilestoneTags(writer, segment);
      writeText(writer, segment);
      writeCloseTags(writer, segment);

    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static StreamingOutput xmlOutputStream(AlexandriaService service, UUID resourceId, String view) {
    if (StringUtils.isNotBlank(view)) {
      TextView textView = service.getTextView(resourceId, view)//
          .orElseThrow(() -> new NotFoundException("No view '" + view + "' found for this resource."));
      return streamTextViewXML(service, resourceId, textView);
    }
    return streamXML(service, resourceId);
  }

  private static void writeOpenTags(Writer writer, TextGraphSegment segment) throws IOException {
    for (TextAnnotation textAnnotation : segment.getTextAnnotationsToOpen()) {
      String name = textAnnotation.getName();
      String openTag = getOpenTag(name, textAnnotation.getAttributes());
      writer.write(openTag);
    }
  }

  private static void writeMilestoneTags(Writer writer, TextGraphSegment segment) throws IOException {
    Optional<TextAnnotation> optionalMilestone = segment.getMilestoneTextAnnotation();
    if (optionalMilestone.isPresent()) {
      TextAnnotation milestone = optionalMilestone.get();
      String milestoneTag = getMilestoneTag(milestone.getName(), milestone.getAttributes());
      writer.write(milestoneTag);
    }
  }

  private static void writeText(Writer writer, TextGraphSegment segment) throws IOException {
    writer.write(segment.getTextSegment());
  }

  private static void writeCloseTags(Writer writer, TextGraphSegment segment) throws IOException {
    for (TextAnnotation textAnnotation : segment.getTextAnnotationsToClose()) {
      String name = textAnnotation.getName();
      String closeTag = getCloseTag(name);
      writer.write(closeTag);
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
    private List<TextAnnotation> overruledTextAnnotations = Lists.newArrayList();

    public TextViewContext(TextView textView) {
      elementViewMap = textView.getElementViewMap();
    }

    public boolean includeTag(String name, TextAnnotation textAnnotation) {
      ElementView defaultElementView = elementViewMap.get(TextViewDefinition.DEFAULT_ATTRIBUTENAME);
      ElementView elementView = elementViewMap.getOrDefault(name, defaultElementView);
      ElementMode elementMode = elementView.getElementMode();
      ElementMode defaultViewElementMode = defaultElementView.getElementMode();
      Optional<AttributePreCondition> preCondition = elementView.getPreCondition();
      boolean includeAccordingToElementMode = elementMode.equals(ElementMode.show);
      boolean preConditionIsMet = preConditionIsMet(preCondition, textAnnotation);
      if (!preConditionIsMet) {
        includeAccordingToElementMode = defaultViewElementMode.equals(ElementMode.show);
      }

      return notInsideIgnoredElement() && includeAccordingToElementMode;
    }

    private boolean preConditionIsMet(Optional<AttributePreCondition> preCondition, TextAnnotation textAnnotation) {
      if (preCondition.isPresent()) {
        if (overruledTextAnnotations.contains(textAnnotation)) {
          return false;
        }
        AttributePreCondition attributePreCondition = preCondition.get();
        String attribute = attributePreCondition.getAttribute();
        List<String> values = attributePreCondition.getValues();
        String actualValue = textAnnotation.getAttributes().get(attribute);
        switch (attributePreCondition.getFunction()) {
        case is:
          return values.contains(actualValue);
        case isNot:
          return !values.contains(actualValue);
        case firstOf:
          // TODO
          break;
        default:
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
      boolean preConditionIsMet = preConditionIsMet(preCondition, textAnnotation);
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

    /**
     * registers TextAnnotations in segment that:
     * - annotate the same textrange
     * - have the same name
     * - that name is used in a firstOf function in the viewDefinition
     * So they can be handled properly in includeTag
     *
     * @param segment
     *          the TextGraphSegment to analyze
     */
    public void registerCompetingTextAnnotations(TextGraphSegment segment) {
      List<String> relevantElementNames = elementViewMap.entrySet().stream()//
          .filter(this::hasFirstOfAttributeFunction)//
          .map(Entry::getKey)//
          .collect(toList());
      Set<TextAnnotation> segmentTextAnnotations = Sets.newHashSet(segment.textAnnotationsToOpen);
      segmentTextAnnotations.addAll(segment.textAnnotationsToClose);
      segmentTextAnnotations.removeIf(ta -> !relevantElementNames.contains(ta.getName()));
      overruledTextAnnotations = segmentTextAnnotations.stream()//
          .collect(groupingBy(this::textAnnotationGrouping))//
          .values()//
          .stream()//
          .map(this::overruledTextAnnotations)//
          .flatMap(List::stream)//
          .collect(toList());
    }
    // TODO: handle consecutive milestones

    private boolean hasFirstOfAttributeFunction(Entry<String, ElementView> entry) {
      Optional<AttributePreCondition> preCondition = entry.getValue().getPreCondition();
      return preCondition.isPresent() && ElementView.AttributeFunction.firstOf.equals(preCondition.get().getFunction());
    }

    private String textAnnotationGrouping(TextAnnotation textAnnotation) {
      // TODO: include annotated textRange
      return textAnnotation.getName();
    }

    private List<TextAnnotation> overruledTextAnnotations(List<TextAnnotation> group) {
      String elementName = group.get(0).getName();
      AttributePreCondition attributePreCondition = elementViewMap.get(elementName)//
          .getPreCondition().get();
      String attribute = attributePreCondition.getAttribute();
      List<String> prioritizedValues = Lists.newArrayList(attributePreCondition.getValues());
      int originalSize = group.size();
      do {
        String value = prioritizedValues.remove(0);
        group.removeIf(ta -> ta.getAttributes().get(attribute).equals(value));
      } while (group.size() == originalSize && !prioritizedValues.isEmpty());
      return group;
    }
  }

  public static void streamTextGraphSegment(Writer writer, TextGraphSegment segment, TextViewContext textViewContext) {
    try {
      textViewContext.registerCompetingTextAnnotations(segment);
      writeOpenTags(writer, segment, textViewContext);
      writeMilestoneTags(writer, segment, textViewContext);
      writeText(writer, segment, textViewContext);
      writeCloseTags(writer, segment, textViewContext);

    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private static void writeCloseTags(Writer writer, TextGraphSegment segment, TextViewContext textViewContext) throws IOException {
    for (TextAnnotation textAnnotation : segment.getTextAnnotationsToClose()) {
      String name = textAnnotation.getName();
      if (textViewContext.includeTag(name, textAnnotation)) {
        String closeTag = getCloseTag(name);
        writer.write(closeTag);
      }
      textViewContext.popWhenIgnoring(textAnnotation);
    }
  }

  private static void writeText(Writer writer, TextGraphSegment segment, TextViewContext textViewContext) throws IOException {
    if (textViewContext.notInsideIgnoredElement()) {
      writeText(writer, segment);
    }
  }

  private static void writeMilestoneTags(Writer writer, TextGraphSegment segment, TextViewContext textViewContext) throws IOException {
    Optional<TextAnnotation> optionalMilestone = segment.getMilestoneTextAnnotation();
    if (optionalMilestone.isPresent()) {
      TextAnnotation milestone = optionalMilestone.get();
      String name = milestone.getName();
      if (textViewContext.includeTag(name, milestone)) {
        String milestoneTag = getMilestoneTag(name, textViewContext.includedAttributes(milestone));
        writer.write(milestoneTag);
      }
    }
  }

  private static void writeOpenTags(Writer writer, TextGraphSegment segment, TextViewContext textViewContext) throws IOException {
    for (TextAnnotation textAnnotation : segment.getTextAnnotationsToOpen()) {
      String name = textAnnotation.getName();
      if (textViewContext.includeTag(name, textAnnotation)) {
        String openTag = getOpenTag(name, textViewContext.includedAttributes(textAnnotation));
        writer.write(openTag);
      }
      textViewContext.pushWhenIgnoring(textAnnotation);
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
