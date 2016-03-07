package nl.knaw.huygens.alexandria.textlocator;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;

import nl.knaw.huygens.tei.QueryableDocument;

public class ByXPathTextLocator implements AlexandriaTextLocator {
  static final String PREFIX = "xpath";
  String xpath;

  public String getXPath() {
    return xpath;
  }

  public void setXPath(String xpath) {
    this.xpath = xpath;
  }

  public AlexandriaTextLocator withXPath(String xpath) {
    setXPath(xpath);
    return this;
  }

  @Override
  public String toString() {
    return PREFIX + ":" + xpath;
  }

  @Override
  public void validate(InputStream textStream) throws TextLocatorValidationException {
    String xml;
    try {
      xml = IOUtils.toString(textStream);
      QueryableDocument qDocument = QueryableDocument.createFromXml(xml, true);
      String result = qDocument.evaluateXPathToString(xpath);
    } catch (IOException | XPathExpressionException e) {
      e.printStackTrace();
      throw new TextLocatorValidationException(e.getMessage());
    }
  }
}
