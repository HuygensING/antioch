package nl.knaw.huygens.alexandria.text;

import java.util.Stack;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.TextHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;

public class XmlVisitor extends DelegatingVisitor<XmlContext> {

  private TextParseResult textParseResult = new TextParseResult();
  private Stack<TextRange> textRangeStack = new Stack<>();

  public XmlVisitor() {
    super(new XmlContext());
    setDefaultElementHandler(defaultElementHandler());
    setTextHandler(textHandler());
  }

  private TextHandler<XmlContext> textHandler() {
    return (text, context) -> {
      TextNode textNode = addNewTextNode(text.getText());
      textRangeStack.stream()//
          .filter(TextRange::hasNoFirstNode)//
          .forEach(textRange -> textRange.setFirstNode(textNode));
      textRangeStack.stream()//
          .forEach(textRange -> textRange.setLastNode(textNode));
      return Traversal.NEXT;
    };
  }

  private TextNode addNewTextNode(String text) {
    TextNode textNode = TextNode.of(text);
    textParseResult.getTextNodes().add(textNode);
    return textNode;
  }

  private ElementHandler<XmlContext> defaultElementHandler() {
    return new ElementHandler<XmlContext>() {

      @Override
      public Traversal enterElement(Element e, XmlContext c) {
        TextRange textRange = new TextRange();
        textRangeStack.push(textRange);
        textParseResult.getTextRanges().add(textRange);
        return Traversal.NEXT;
      }

      @Override
      public Traversal leaveElement(Element e, XmlContext c) {
        TextRange textRange = textRangeStack.pop();
        if (e.hasNoChildren()) { // milestone element
          TextNode textNode = addNewTextNode("");
          textRange.setFirstNode(textNode).setLastNode(textNode);
        }
        Log.info("E:{} -> TR:<{}>..<{}>", e.getName(), textRange.getFirstNode().getText(), textRange.getLastNode().getText());
        // TODO! koppel aan element
        return Traversal.NEXT;
      }
    };
  }

  public TextParseResult getParseResult() {
    return textParseResult;
  }

}
