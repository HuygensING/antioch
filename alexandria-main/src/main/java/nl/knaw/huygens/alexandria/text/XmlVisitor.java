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
  private boolean appendTextNode = false;

  public XmlVisitor() {
    super(new XmlContext());
    setDefaultElementHandler(defaultElementHandler());
    setTextHandler(textHandler());
  }

  private TextHandler<XmlContext> textHandler() {
    return (text, context) -> {
      String textContent = text.getText();
      if (appendTextNode) {
        // append adjacent texts, since the saxparser will sometimes split up text
<<<<<<< a071400cb7b3e729ed003b5cca4a3383e682ce7d
        textParseResult.getLastTextNode().appendText(textContent);
=======
        List<TextNode> textNodes = textParseResult.getTextNodes();
        TextNode textNode = textNodes.get(textNodes.size() - 1);
        textNode.appendText(textContent);
>>>>>>> use correct term

      } else {
        appendTextNode = true;
        TextNode textNode = addNewTextNode(textContent);
        textRangeStack.stream()//
            .filter(TextRange::hasNoFirstNode)//
            .forEach(textRange -> textRange.setFirstNode(textNode));
        textRangeStack.stream()//
            .forEach(textRange -> textRange.setLastNode(textNode));
      }
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
        appendTextNode = false;
        TextRange textRange = new TextRange();
        textRangeStack.push(textRange);
        textParseResult.getTextRanges().add(textRange);
        return Traversal.NEXT;
      }

      @Override
      public Traversal leaveElement(Element e, XmlContext c) {
        appendTextNode = false;
        TextRange textRange = textRangeStack.pop();
        if (e.hasNoChildren()) { // milestone element
          TextNode textNode = addNewTextNode("");
          textRange.setFirstNode(textNode).setLastNode(textNode);
        }
        Tag tag = new Tag().setName(e.getName()).setAttributes(e.getAttributes());
        Log.info("{} -> {}", tag, textRange);
        textParseResult.getTag2TextRangeMap().put(tag, textRange);
        return Traversal.NEXT;
      }
    };
  }

  public TextParseResult getParseResult() {
    return textParseResult;
  }

}
