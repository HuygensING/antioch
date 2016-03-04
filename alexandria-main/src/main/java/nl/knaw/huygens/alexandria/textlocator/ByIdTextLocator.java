package nl.knaw.huygens.alexandria.textlocator;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;

import nl.knaw.huygens.tei.QueryableDocument;

public class ByIdTextLocator implements AlexandriaTextLocator {
  static final String PREFIX = "id";
  String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public AlexandriaTextLocator withId(String id) {
    setId(id);
    return this;
  }

  @Override
  public String toString() {
    return PREFIX + ":" + id;
  }

  @Override
  public void validate(InputStream textStream) throws TextLocatorValidationException {
    try {
      String xml = IOUtils.toString(textStream);
      QueryableDocument qDocument = QueryableDocument.createFromXml(xml, true);
      Boolean idExists = qDocument.evaluateXPathToBoolean("boolean(//*[@xml:id=\"" + id + "\"])");
      if (!idExists) {
        throw new TextLocatorValidationException("The resource text has no element with xml:id=\"" + id + "\"");
      }
    } catch (IOException | XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }

}
