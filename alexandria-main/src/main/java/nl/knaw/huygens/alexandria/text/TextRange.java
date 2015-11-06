package nl.knaw.huygens.alexandria.text;

public class TextRange {
  TextNode firstNode;
  TextNode lastNode;

  public TextNode getFirstNode() {
    return firstNode;
  }

  public TextRange setFirstNode(TextNode firstNode) {
    this.firstNode = firstNode;
    return this;
  }

  public TextNode getLastNode() {
    return lastNode;
  }

  public TextRange setLastNode(TextNode lastNode) {
    this.lastNode = lastNode;
    return this;
  }

  public boolean hasNoFirstNode() {
    return firstNode == null;
  }

  public boolean isEmpty() {
    return (firstNode == lastNode) && firstNode.isEmpty();
  }
}
