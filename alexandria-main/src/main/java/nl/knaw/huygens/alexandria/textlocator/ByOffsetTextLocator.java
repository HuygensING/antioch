package nl.knaw.huygens.alexandria.textlocator;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;

import nl.knaw.huygens.tei.QueryableDocument;

public class ByOffsetTextLocator implements AlexandriaTextLocator {
  static final String PREFIX = "offset";
  Long start = 0L;
  Long length = 0L;

  public ByOffsetTextLocator(Long start, Long length) {
    setStart(start);
    setLength(length);
  }

  // public AlexandriaTextLocator withStart(Long start) {
  // setStart(start);
  // return this;
  // }

  public Long getStart() {
    return start;
  }

  public void setStart(Long start) {
    this.start = start;
  }

  // public AlexandriaTextLocator withLength(Long length) {
  // setLength(length);
  // return this;
  // }

  public Long getLength() {
    return length;
  }

  public void setLength(Long length) {
    this.length = length;
  }

  @Override
  public String toString() {
    return PREFIX + ":" + start + "," + length;
  }

  @Override
  public void validate(InputStream textStream) throws TextLocatorValidationException {
    String xml;
    try {
      xml = IOUtils.toString(textStream);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    QueryableDocument qDocument = QueryableDocument.createFromXml(xml, true);
    try {
      qDocument.evaluateXPathToString("substring(/," + start + "," + length + ")");
    } catch (XPathExpressionException e) {
      e.printStackTrace();
      throw new TextLocatorValidationException("The offset (" + start + "," + length + ") is not valid for the resource text.");
    }
  }

}
