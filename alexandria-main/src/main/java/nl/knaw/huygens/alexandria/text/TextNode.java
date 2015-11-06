package nl.knaw.huygens.alexandria.text;

public class TextNode {
  private String text;

  private TextNode() {
  }

  public static TextNode of(String text) {
    return new TextNode().setText(text);
  }

  private TextNode setText(String text) {
    this.text = text;
    return this;
  }

  public String getText() {
    return this.text;
  }

  public boolean isEmpty() {
    return "".equals(text);
  }

  @Override
  public String toString() {
    return "TextNode:(\"" + text.replace("\n", "\\n").replace("\"", "\\\"") + "\")";
  }

  public void appendText(String textToAppend) {
    text = text + textToAppend;
  }
}
