package nl.knaw.huygens.alexandria.textgraph;

import java.util.Stack;

import nl.knaw.huygens.tei.Comment;
import nl.knaw.huygens.tei.CommentHandler;
import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.ProcessingInstruction;
import nl.knaw.huygens.tei.ProcessingInstructionHandler;
import nl.knaw.huygens.tei.Text;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.handlers.XmlTextHandler;

public class XmlVisitor extends DelegatingVisitor<XmlContext> implements CommentHandler<XmlContext>, ElementHandler<XmlContext>, ProcessingInstructionHandler<XmlContext> {

  private static ParseResult result;
  private Stack<Integer> startIndexStack = new Stack<>();
  private Stack<Element> elementStack = new Stack<>();
  private static boolean lastNodeWasText = false;

  public XmlVisitor(ParseResult result) {
    super(new XmlContext());
    XmlVisitor.result = result;
    setDefaultElementHandler(this);
    setCommentHandler(this);
    setProcessingInstructionHandler(this);
    setTextHandler(new TextSegmentHandler());
  }

  @Override
  public Traversal enterElement(Element element, XmlContext context) {
    elementStack.add(element);
    lastNodeWasText = false;
    startIndexStack.push(result.getTextSegments().size());
    if (element.hasNoChildren()) {
      result.getTextSegments().add("");
    }
    return Traversal.NEXT;
  }

  @Override
  public Traversal leaveElement(Element element, XmlContext context) {
    elementStack.pop();
    lastNodeWasText = false;
    XmlAnnotation xmlAnnotation = new XmlAnnotation(element.getName(), element.getAttributes(), elementStack.size())//
        .setMilestone(element.hasNoChildren())//
        .setFirstSegmentIndex(startIndexStack.pop())//
        .setLastSegmentIndex(result.getTextSegments().size() - 1)//
        ;
    result.getXmlAnnotations().add(xmlAnnotation);
    return Traversal.NEXT;
  }

  @Override
  public Traversal visitProcessingInstruction(ProcessingInstruction processingInstruction, XmlContext context) {
    return Traversal.NEXT;
  }

  @Override
  public Traversal visitComment(Comment comment, XmlContext context) {
    return Traversal.NEXT;
  }

  public static class TextSegmentHandler extends XmlTextHandler<XmlContext> {
    @Override
    public Traversal visitText(Text text, XmlContext context) {
      String filteredText = filterText(text.getText());
      if (lastNodeWasText) {
        int lastIndex = result.getTextSegments().size() - 1;
        String segment = result.getTextSegments().get(lastIndex);
        result.getTextSegments().set(lastIndex, segment + filteredText);
      } else {
        result.getTextSegments().add(filteredText);
      }
      lastNodeWasText = true;
      return Traversal.NEXT;
    }
  }

}
