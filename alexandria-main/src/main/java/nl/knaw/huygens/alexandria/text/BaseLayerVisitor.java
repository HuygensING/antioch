package nl.knaw.huygens.alexandria.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.BaseLayerDefinition;
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
  List<AnnotationData> annotationData = new ArrayList<>();
  static List<String> annotationActions = new ArrayList<>();

  static ElementTally elementTally = new ElementTally();

  static Stack<Element> baseElementStack = new Stack<>();

  public List<String> getAnnotationActions() {
    return annotationActions;
  }

  public BaseLayerVisitor(BaseLayerDefinition baseLayerDefinition) {
    super();
    setCommentHandler(this);
    setTextHandler(new XmlTextHandler<>());
    setDefaultElementHandler(this);
    setProcessingInstructionHandler(this);
    baseLayerDefinition.getBaseElementDefinitions().forEach(bed -> {
      addElementHandler(new BaseElementHandler(bed.getBaseAttributes()), bed.getName());
    });
  }

  @Override
  public Traversal enterElement(Element element, XmlContext context) {
    elementTally.tally(element);
    context.openLayer();
    return Traversal.NEXT;
  }

  @Override
  public Traversal leaveElement(Element element, XmlContext context) {
    String annotatedBaseText = context.closeLayer();
    context.addLiteral(annotatedBaseText);
    annotationActions.add("element annotation on '" + annotatedBaseText + "':element=" + element.getName() + element.getAttributes());
    // AnnotationData aData = new AnnotationData();
    // aData.setAnnotatedBaseText(annotatedBaseText);
    // aData.setLevel(XmlAnnotationLevel.element);
    // aData.setType(element.getName());
    // aData.setValue(element);
    return Traversal.NEXT;
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
    }

    @Override
    public Traversal enterElement(Element element, XmlContext context) {
      elementTally.tally(element);
      Element base = new Element(element.getName());
      element.getAttributes().forEach((key, value) -> {
        if (baseAttributes.contains(key)) {
          base.setAttribute(key, value);
        } else {
          // TODO: annotation
          annotationActions.add("attribute annotation on '" + element.getName() + "':" + key + "=" + value);
        }
      });
      if (!baseElementStack.isEmpty()) {
        base.setParent(baseElementStack.peek());
      }
      logXPath(base);
      baseElementStack.push(base);
      context.addOpenTag(base);
      context.openLayer();
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, XmlContext context) {
      context.addLiteral(context.closeLayer());
      context.addCloseTag(element);
      baseElementStack.pop();
      return Traversal.NEXT;
    }

    private void logXPath(Element element) {
      Log.info("xpath={}", xpath(element));
    }
  }

  public String getBaseLayerData() {
    elementTally.logReport();
    return getContext().getResult();
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
