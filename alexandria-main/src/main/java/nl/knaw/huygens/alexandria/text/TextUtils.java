package nl.knaw.huygens.alexandria.text;

<<<<<<< 39c96eabda13be48f8df7cfd776165e816f9f875
import nl.knaw.huygens.Log;
=======
>>>>>>> [NLA-132] parse xml to determine textnodes, textranges
import nl.knaw.huygens.tei.Document;

public class TextUtils {
  public static TextParseResult parse(String xml) {
    XmlVisitor visitor = new XmlVisitor();
<<<<<<< 39c96eabda13be48f8df7cfd776165e816f9f875
    try {
      Document.createFromXml(xml, true).accept(visitor);
    } catch (RuntimeException rte) {
      Log.error("parsing error: {}", rte);
      return new TextParseResult().setParseError(rte.getMessage());
    }
=======
    Document.createFromXml(xml).accept(visitor);
>>>>>>> [NLA-132] parse xml to determine textnodes, textranges
    return visitor.getParseResult();
  }
}
