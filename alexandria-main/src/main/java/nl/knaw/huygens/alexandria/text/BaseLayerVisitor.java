package nl.knaw.huygens.alexandria.text;

import java.util.List;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;
import nl.knaw.huygens.tei.Comment;
import nl.knaw.huygens.tei.CommentHandler;
import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.ProcessingInstruction;
import nl.knaw.huygens.tei.ProcessingInstructionHandler;
import nl.knaw.huygens.tei.Text;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.handlers.XmlTextHandler;

public class BaseLayerVisitor extends DelegatingVisitor<BLVContext> implements CommentHandler<BLVContext>, ElementHandler<BLVContext>, ProcessingInstructionHandler<BLVContext> {
  private String id = "";

  public BaseLayerVisitor(BLVContext blContext, BaseLayerDefinition baseLayerDefinition, String id) {
    super(blContext);
    this.id = id;
    setCommentHandler(this);
    setTextHandler(new OffsetXmlTextHandler());
    setDefaultElementHandler(this);
    setProcessingInstructionHandler(this);
    baseLayerDefinition.getBaseElementDefinitions().forEach(bed -> addElementHandler(new BaseElementHandler(bed.getBaseAttributes()), bed.getName()));
    addElementHandler(new SubTextPlaceholderHandler(), TextUtil.SUBTEXTPLACEHOLDER);
  }

  // non-base elements
  @Override
  public Traversal enterElement(Element element, BLVContext context) {
    context.getElementTally().tally(element);
    if (element.getParent() == null) {
      context.getValidationErrors().add("Validation error: root element <" + element.getName() + "> is not in the base layer definition.");
    }
    context.getTextOffsetStack().push(context.getTextOffset());
    context.openLayer();
    return Traversal.NEXT;
  }

  @Override
  public Traversal leaveElement(Element element, BLVContext context) {
    String annotatedBaseText = context.closeLayer();
    context.addLiteral(annotatedBaseText);
    String xpath = context.substringOffsetXPath(context.getTextOffsetStack().pop());
    Log.info("xpath={}", xpath);
    context.getAnnotationData()
        .add(new AnnotationData()//
            .setAnnotatedBaseText(annotatedBaseText)//
            .setLevel(XmlAnnotationLevel.element)//
            .setType(element.getName())//
            .setValue(element)//
            .setXPath(xpath));
    return Traversal.NEXT;
  }

  @Override
  public Traversal visitComment(Comment comment, BLVContext context) {
    Log.warn("unprocessed comment: {}", comment.getComment());
    return Traversal.NEXT;
  }

  @Override
  public Traversal visitProcessingInstruction(ProcessingInstruction processingInstruction, BLVContext context) {
    Log.warn("unprocessed processing instruction: {}", processingInstruction);
    return Traversal.NEXT;
  }

  static class BaseElementHandler implements ElementHandler<BLVContext> {
    private List<String> baseAttributes;

    public BaseElementHandler(List<String> baseAttributes) {
      this.baseAttributes = baseAttributes;
      if (!baseAttributes.contains(TextUtil.XML_ID)) {
        baseAttributes.add(0, TextUtil.XML_ID);
      }
    }

    @Override
    public Traversal enterElement(Element element, BLVContext context) {
      context.getElementTally().tally(element);
      Element base = new Element(element.getName());

      // use attribute order as defined in baselayer definition
      baseAttributes.stream()//
          .filter(element::hasAttribute)//
          .forEach(key -> base.setAttribute(key, element.getAttribute(key)));
      element.getAttributes().forEach((key, value) -> {
        if (!baseAttributes.contains(key)) {
          context.getAnnotationData()
              .add(new AnnotationData()//
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
    public Traversal leaveElement(Element element, BLVContext context) {
      context.addCloseTag(element);
      return Traversal.NEXT;
    }

    private void logXPath(Element element) {
      Log.info("xpath={}", TextUtil.xpath(element));
    }
  }

  static class SubTextPlaceholderHandler implements ElementHandler<BLVContext> {
    @Override
    public Traversal enterElement(Element element, BLVContext context) {
      String substringXPath = context.substringOffsetXPath(context.getTextOffset());
      context.getSubresourceXPathMap().put(element.getAttribute(TextUtil.XML_ID), substringXPath);
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, BLVContext context) {
      return Traversal.NEXT;
    }
  }

  static class OffsetXmlTextHandler extends XmlTextHandler<BLVContext> {
    @Override
    public Traversal visitText(Text text, BLVContext context) {
      String content = filterText(text.getText());
      context.addLiteral(content);
      context.addToTextOffset(content.length());
      return Traversal.NEXT;
    }

  }

  public BaseLayerData getBaseLayerData() {
    getContext().getElementTally().logReport();
    return BaseLayerData//
        .withBaseLayer(getContext().getResult())//
        .withValidationErrors(getContext().getValidationErrors())//
        .withId(id)//
        .withAnnotationData(getContext().getAnnotationData());
  }

}
