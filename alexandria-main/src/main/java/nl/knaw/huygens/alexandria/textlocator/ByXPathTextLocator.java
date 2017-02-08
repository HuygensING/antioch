package nl.knaw.huygens.alexandria.textlocator;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
