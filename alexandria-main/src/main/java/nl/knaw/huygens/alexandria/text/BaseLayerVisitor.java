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
  static List<AnnotationData> annotationData = new ArrayList<>();
  // static List<String> annotationActions = new ArrayList<>();
  static ElementTally elementTally = new ElementTally();
  static Stack<Element> baseElementStack = new Stack<>();

  // public List<String> getAnnotationActions() {
  // return annotationActions;
  // }

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

  // non-base elements
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
    // String attributeAnnotationActions = element.getAttributes().entrySet().stream()//
    // .map(kv -> " attribute annotation '" + kv.getKey() + "'='" + kv.getValue() + "'")//
    // .collect(Collectors.joining(", and ", ", with ", ""));
    //
    // annotationActions.add(MessageFormat.format(//
    // "adding element annotation on ''{0}'' in base element '{}' element={1}", //
    // annotatedBaseText, //
    // xpath(baseElementStack.peek()), element.getName(), //
    // attributeAnnotationActions)//
    // );
    annotationData.add(new AnnotationData()//
        .setAnnotatedBaseText(annotatedBaseText)//
        .setLevel(XmlAnnotationLevel.element)//
        .setType(element.getName())//
        .setValue(element)//
        .setXPath(xpath(baseElementStack.peek())));
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
          annotationData.add(new AnnotationData()//
              .setAnnotatedBaseText("")//
              .setLevel(XmlAnnotationLevel.attribute)//
              .setType(key)//
              .setValue(value)//
              .setXPath(xpath(base)));
          // annotationActions.add("adding attribute annotation on '" + xpath(base) + "' : " + key + "=" + value);
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

  public BaseLayerData getBaseLayerData() {
    elementTally.logReport();
    return BaseLayerData//
        .withBaseLayer(getContext().getResult())//
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
