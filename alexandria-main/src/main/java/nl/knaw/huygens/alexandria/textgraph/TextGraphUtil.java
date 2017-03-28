package nl.knaw.huygens.alexandria.textgraph;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.alexandria.api.model.text.view.AttributePreCondition;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeFunction;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeMode;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.ElementMode;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.tei.Document;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class TextGraphUtil {

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
      XmlStreamContext context = new XmlStreamContext(writer);
      Consumer<TextGraphSegment> action = segment -> streamTextGraphSegment(context, segment);
      stream(service, resourceId, writer, action, new ArrayList<List<String>>());
    };
  }

  public static void streamTextGraphSegment(XmlStreamContext context, TextGraphSegment segment) {
    try {
      writeOpenTags(context, segment);
      writeMilestoneTags(context.getWriter(), segment);
      writeText(context.getWriter(), segment);
      writeCloseTags(context, segment);

    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static StreamingOutput xmlOutputStream(AlexandriaService service, UUID resourceId, String viewName) {
    Map<String, String> viewParameters = new HashMap<>();
    return xmlOutputStream(service, resourceId, viewName, viewParameters);
  }

  public static StreamingOutput xmlOutputStream(AlexandriaService service, UUID resourceId, String viewName, Map<String, String> viewParameters) {
    if (StringUtils.isNotBlank(viewName)) {
      TextView textView = service.getTextView(resourceId, viewName)//
          .orElseThrow(() -> new NotFoundException("No view '" + viewName + "' found for this resource."));
      textView.substitute(viewParameters);
      // Log.info("textView={}", textView);
      return streamTextViewXML(service, resourceId, textView);
    }
    return streamXML(service, resourceId);
  }

  private static void writeOpenTags(XmlStreamContext context, TextGraphSegment segment) throws IOException {
    for (TextAnnotation textAnnotation : segment.getTextAnnotationsToOpen()) {
      String name = textAnnotation.getName();
      context.openTag(name);
      String openTag = getOpenTag(name, textAnnotation.getAttributes());
      context.getWriter().write(openTag);
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

  private static void writeCloseTags(XmlStreamContext context, TextGraphSegment segment) throws IOException {
    for (TextAnnotation textAnnotation : segment.getTextAnnotationsToClose()) {
      String name = textAnnotation.getName();
      context.closeTag(name);
      String closeTag = getCloseTag(name);
      context.getWriter().write(closeTag);
    }
  }

  public static StreamingOutput streamTextViewXML(AlexandriaService service, UUID resourceId, TextView textView) {
    return output -> {
      Writer writer = createBufferedUTF8OutputStreamWriter(output);
      TextViewContext textViewContext = new TextViewContext(textView);
      Consumer<TextGraphSegment> action = segment -> streamTextGraphSegment(writer, segment, textViewContext);
      stream(service, resourceId, writer, action, textView.getOrderedLayerTags());
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
        boolean preConditionIsFirstOfFunction = preCondition.isPresent() //
            && AttributeFunction.firstOf.equals(preCondition.get().getFunction());
        includeAccordingToElementMode = !preConditionIsFirstOfFunction && defaultViewElementMode.equals(ElementMode.show);
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
        group.removeIf(ta -> value.equals(ta.getAttributes().get(attribute)));
      } while (group.size() == originalSize && !prioritizedValues.isEmpty());
      return group;
    }

    public List<TextAnnotation> getOrderedTextAnnotationsToOpen(TextGraphSegment segment) {
      List<TextAnnotation> textAnnotationsToOpen = segment.getTextAnnotationsToOpen();
      textAnnotationsToOpen.forEach(textAnnotation -> {
      });
      return textAnnotationsToOpen;
    }

    public List<TextAnnotation> getOrderedTextAnnotationsToClose(TextGraphSegment segment) {
      List<TextAnnotation> textAnnotationsToClose = segment.getTextAnnotationsToClose();
      return textAnnotationsToClose;
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
    List<TextAnnotation> textAnnotationsToOpen = textViewContext.getOrderedTextAnnotationsToOpen(segment);
    for (TextAnnotation textAnnotation : textAnnotationsToOpen) {
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

  /* private methods */

  private static Writer createBufferedUTF8OutputStreamWriter(OutputStream output) throws UnsupportedEncodingException {
    return new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
  }

  private static void stream(AlexandriaService service, UUID resourceId, Writer writer, Consumer<TextGraphSegment> action, List<List<String>> orderedLayerTags) throws IOException {
    service.runInTransaction(() -> service.getTextGraphSegmentStream(resourceId, orderedLayerTags).forEach(action));
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

}
