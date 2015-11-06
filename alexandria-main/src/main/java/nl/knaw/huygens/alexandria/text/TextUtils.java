package nl.knaw.huygens.alexandria.text;

import nl.knaw.huygens.tei.Document;

public class TextUtils {
  public static TextParseResult parse(String xml) {
    XmlVisitor visitor = new XmlVisitor();
    Document.createFromXml(xml).accept(visitor);
    return visitor.getParseResult();
  }
}
