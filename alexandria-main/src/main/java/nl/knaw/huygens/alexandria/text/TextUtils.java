package nl.knaw.huygens.alexandria.text;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.tei.Document;

public class TextUtils {
  public static TextParseResult parse(String xml) {
    XmlVisitor visitor = new XmlVisitor();
    try {
      Document.createFromXml(xml, true).accept(visitor);
    } catch (RuntimeException rte) {
      Log.error("parsing error: {}", rte);
      return new TextParseResult().setParseError(rte.getMessage());
    }
    return visitor.getParseResult();
  }
}
