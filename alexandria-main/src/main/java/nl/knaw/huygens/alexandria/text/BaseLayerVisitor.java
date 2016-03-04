package nl.knaw.huygens.alexandria.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;
import nl.knaw.huygens.tei.Comment;
import nl.knaw.huygens.tei.CommentHandler;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.ProcessingInstruction;
import nl.knaw.huygens.tei.ProcessingInstructionHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.export.ExportVisitor;
import nl.knaw.huygens.tei.handlers.XmlTextHandler;

public class BaseLayerVisitor extends ExportVisitor implements CommentHandler<XmlContext>, ElementHandler<XmlContext>, ProcessingInstructionHandler<XmlContext> {
  private static List<AnnotationData> annotationData = new ArrayList<>();
  private static ElementTally elementTally = new ElementTally();
  private static final List<String> validationErrors = new ArrayList<>();
  private static Map<String, String> subresourceXPathMap = new HashMap<>();
  private static Long textOffset = 1L;
  private static Stack<Long> textOffsetStack = new Stack<>();

  public BaseLayerVisitor(BaseLayerDefinition baseLayerDefinition) {
    super();
    setCommentHandler(this);
    setTextHandler(new OffsetXmlTextHandler());
    setDefaultElementHandler(this);
    setProcessingInstructionHandler(this);
    baseLayerDefinition.getBaseElementDefinitions().forEach(bed -> addElementHandler(new BaseElementHandler(bed.getBaseAttributes()), bed.getName()));
    addElementHandler(new SubTextPlaceholderHandler(), TextUtil.SUBTEXTPLACEHOLDER);
    validationErrors.clear();
    annotationData.clear();
    subresourceXPathMap.clear();
    elementTally = new ElementTally();
    textOffset = 1L;
  }

  // non-base elements
  @Override
  public Traversal enterElement(Element element, XmlContext context) {
    elementTally.tally(element);
    if (element.getParent() == null) {
      validationErrors.add("Validation error: root element <" + element.getName() + "> is not in the base layer definition.");
    }
    textOffsetStack.push(textOffset);
    context.openLayer();
    return Traversal.NEXT;
  }

  @Override
  public Traversal leaveElement(Element element, XmlContext context) {
    String annotatedBaseText = context.closeLayer();
    context.addLiteral(annotatedBaseText);
    String xpath = substringOffsetXPath(textOffsetStack.pop());
    Log.info("xpath={}", xpath);
    annotationData.add(new AnnotationData()//
        .setAnnotatedBaseText(annotatedBaseText)//
        .setLevel(XmlAnnotationLevel.element)//
        .setType(element.getName())//
        .setValue(element)//
        .setXPath(xpath));
    return Traversal.NEXT;
  }

  private static String substringOffsetXPath(Long start) {
    long length = textOffset - start;
    return "substring(/," + start + "," + length + ")";
  }

  @Override
  public Traversal visitComment(Comment comment, XmlContext context) {
    Log.warn("unprocessed comment: {}", comment.getComment());
    return Traversal.NEXT;
  }

  @Override
  public Traversal visitProcessingInstruction(ProcessingInstruction processingInstruction, XmlContext context) {
    Log.warn("unprocessed processing instruction: {}", processingInstruction);
    return Traversal.NEXT;
  }

  static class BaseElementHandler implements ElementHandler<XmlContext> {
    private List<String> baseAttributes;

    public BaseElementHandler(List<String> baseAttributes) {
      this.baseAttributes = baseAttributes;
      if (!baseAttributes.contains(TextUtil.XML_ID)) {
        baseAttributes.add(0, TextUtil.XML_ID);
      }
    }

    @Override
    public Traversal enterElement(Element element, XmlContext context) {
      elementTally.tally(element);
      Element base = new Element(element.getName());

      // use attribute order as defined in baselayer definition
      baseAttributes.stream()//
          .filter(element::hasAttribute)//
          .forEach(key -> base.setAttribute(key, element.getAttribute(key)));
      element.getAttributes().forEach((key, value) -> {
        if (!baseAttributes.contains(key)) {
          annotationData.add(new AnnotationData()//
              .setAnnotatedBaseText("")//
              .setLevel(XmlAnnotationLevel.attribute)//
              .setType(key)//
              .setValue(value)//
              .setXPath(TextUtil.xpath(base)));
        }
      });
      logXPath(base);
      context.addOpenTag(base);
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, XmlContext context) {
      context.addCloseTag(element);
      return Traversal.NEXT;
    }

    private void logXPath(Element element) {
      Log.info("xpath={}", TextUtil.xpath(element));
    }
  }

  static class SubTextPlaceholderHandler implements ElementHandler<XmlContext> {
    @Override
    public Traversal enterElement(Element element, XmlContext context) {
      String substringXPath = substringOffsetXPath(textOffset);
      subresourceXPathMap.put(element.getAttribute(TextUtil.XML_ID), substringXPath);
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, XmlContext context) {
      return Traversal.NEXT;
    }
  }

  static class OffsetXmlTextHandler extends XmlTextHandler<XmlContext> {
    @Override
    protected String filterText(String text) {
      textOffset += text.length();
      return super.filterText(text);
    }
  }

  public BaseLayerData getBaseLayerData() {
    elementTally.logReport();
    return BaseLayerData//
        .withBaseLayer(getContext().getResult())//
        .withValidationErrors(validationErrors)//
        .withAnnotationData(annotationData);
  }

  public Map<String, String> getSubresourceXPathMap() {
    return subresourceXPathMap;
  }

}
