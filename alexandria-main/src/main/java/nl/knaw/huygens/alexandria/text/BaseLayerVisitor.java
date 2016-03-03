package nl.knaw.huygens.alexandria.text;

import java.util.ArrayList;
import java.util.List;
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
  private static final String XML_ID = "xml:id";
  private static List<AnnotationData> annotationData = new ArrayList<>();
  private static ElementTally elementTally = new ElementTally();
  private static Stack<Element> baseElementStack = new Stack<>();
  private static Stack<Integer> baseElementStartStack = new Stack<>();
  private static Stack<Integer> annotatedTextStartStack = new Stack<>();
  private static final List<String> validationErrors = new ArrayList<>();

  public BaseLayerVisitor(BaseLayerDefinition baseLayerDefinition) {
    super();
    setCommentHandler(this);
    setTextHandler(new XmlTextHandler<>());
    setDefaultElementHandler(this);
    setProcessingInstructionHandler(this);
    baseLayerDefinition.getBaseElementDefinitions().forEach(bed -> addElementHandler(new BaseElementHandler(bed.getBaseAttributes()), bed.getName()));
    validationErrors.clear();
    annotationData.clear();
    elementTally = new ElementTally();
  }

  // non-base elements
  @Override
  public Traversal enterElement(Element element, XmlContext context) {
    elementTally.tally(element);
    if (element.getParent() == null) {
      validationErrors.add("Validation error: root element <" + element.getName() + "> is not in the base layer definition.");
      baseElementStack.push(new Element(""));
      annotatedTextStartStack.push(0);
    } else {
      String result = context.getResult();
      int start = result.length();
      if (!annotatedTextStartStack.isEmpty()) {
        start += annotatedTextStartStack.peek();
      }
      annotatedTextStartStack.push(start);
    }
    context.openLayer();
    return Traversal.NEXT;
  }

  @Override
  public Traversal leaveElement(Element element, XmlContext context) {
    String annotatedBaseText = context.closeLayer();
    context.addLiteral(annotatedBaseText);
    String xpath = substringXPath(annotatedBaseText);
    annotationData.add(new AnnotationData()//
        .setAnnotatedBaseText(annotatedBaseText)//
        .setLevel(XmlAnnotationLevel.element)//
        .setType(element.getName())//
        .setValue(element)//
        .setXPath(xpath));
    return Traversal.NEXT;
  }

  private String substringXPath(String annotatedBaseText) {
    Integer parentBaseElementOffset = baseElementStartStack.isEmpty() ? 0 : baseElementStartStack.peek();
    Integer annotatedTextStart = annotatedTextStartStack.pop();
    int start = annotatedTextStart - parentBaseElementOffset + 1;
    int length = annotatedBaseText.length();
    Log.info("offset: (start={},length={})", start, length);
    return "substring(" + xpath(baseElementStack.peek()) + "," + start + "," + length + ")";
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
      if (!baseAttributes.contains(XML_ID)) {
        baseAttributes.add(0, XML_ID);
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
              .setXPath(xpath(base)));
        }
      });
      if (!baseElementStack.isEmpty()) {
        base.setParent(baseElementStack.peek());
      }
      logXPath(base);
      baseElementStack.push(base);
      context.addOpenTag(base);
      baseElementStartStack.push(context.getResult().length());
      // context.openLayer();
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, XmlContext context) {
      // context.addLiteral(context.closeLayer());
      context.addCloseTag(element);
      baseElementStartStack.pop();
      baseElementStack.pop();
      return Traversal.NEXT;
    }

    private void logXPath(Element element) {
      Log.info("xpath={}", xpath(element));
    }
  }

  public BaseLayerData getBaseLayerData() {
    elementTally.logReport();
    return BaseLayerData//
        .withBaseLayer(getContext().getResult())//
        .withValidationErrors(validationErrors)//
        .withAnnotationData(annotationData);
  }

  public static String xpath(Element element) {
    String xpath = "";
    if (element.hasAttribute(XML_ID)) {
      xpath = "//" + element.getName() + "[@xml:id='" + element.getAttribute(XML_ID) + "']";
    } else {
      Element parent = element.getParent();
      if (parent == null) {
        xpath = "/" + element.getName();
      } else {
        xpath = xpath(parent) + "/" + element.getName();
      }
    }
    return xpath;
  }

}
