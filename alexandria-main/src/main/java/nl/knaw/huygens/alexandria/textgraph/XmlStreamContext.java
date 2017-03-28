package nl.knaw.huygens.alexandria.textgraph;

import nl.knaw.huygens.alexandria.exception.IllegalOverlapException;

import java.io.Writer;
import java.util.Stack;

public class XmlStreamContext {
  private final Writer writer;
  Stack<String> openTags = new Stack<>();

  public XmlStreamContext(Writer writer) {
    this.writer = writer;
  }

  public Writer getWriter() {
    return writer;
  }

  public void openTag(String tag) {
    openTags.push(tag);
  }

  public void closeTag(String tag) {
    String lastOpenedTag = openTags.pop();
    if (!tag.equals(lastOpenedTag)) {
      throw new IllegalOverlapException("Cannot close " + tag + " before " + lastOpenedTag + " is closed first.");
    }
  }

}
