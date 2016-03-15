package nl.knaw.huygens.alexandria.text;

import nl.knaw.huygens.alexandria.test.AlexandriaTest;
import nl.knaw.huygens.tei.Document;
import nl.knaw.huygens.tei.Visitor;

public abstract class AlexandriaVisitorTest extends AlexandriaTest {
  protected void visitXml(String xml, Visitor visitor) {
    Document document = Document.createFromXml(xml, true);
    document.accept(visitor);
  }
}
