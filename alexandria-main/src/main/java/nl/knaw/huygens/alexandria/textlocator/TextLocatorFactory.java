package nl.knaw.huygens.alexandria.textlocator;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;

import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.tei.QueryableDocument;

public class TextLocatorFactory {

  private AlexandriaService service;

  @Inject
  public TextLocatorFactory(AlexandriaService service) {
    this.service = service;
  }

  public AlexandriaTextLocator fromString(String locatorString) throws TextLocatorParseException {
    String[] parts = locatorString.split(":", 2);
    String prefix = parts[0];
    if (ByIdTextLocator.PREFIX.equals(prefix)) {
      return new ByIdTextLocator().withId(parts[1]);
    }
    throw new TextLocatorParseException("The locator prefix '" + prefix + "' is not a valid prefix. Valid prefix: 'id'.");
  }

  public void validate(AlexandriaTextLocator locator, AlexandriaResource resource) {
    if (locator instanceof ByIdTextLocator) {
      ByIdTextLocator byId = (ByIdTextLocator) locator;
      String id = byId.getId();
      Optional<InputStream> textStream = service.getResourceTextAsStream(resource.getId());//
      try {
        String xml = IOUtils.toString(textStream.get());
        QueryableDocument qDocument = QueryableDocument.createFromXml(xml, true);
        Boolean idExists = qDocument.evaluateXPathToBoolean("boolean(//*[@xml:id=\"" + id + "\"])");
        if (!idExists) {
          throw new BadRequestException("The resource text has no element with xml:id=\"" + id + "\"");
        }

      } catch (IOException | XPathExpressionException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
