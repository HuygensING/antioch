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

public class ByIdTextLocator implements AlexandriaTextLocator {
  static final String PREFIX = "id";
  private String id;

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
