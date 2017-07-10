package nl.knaw.huygens.alexandria.textlocator;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
