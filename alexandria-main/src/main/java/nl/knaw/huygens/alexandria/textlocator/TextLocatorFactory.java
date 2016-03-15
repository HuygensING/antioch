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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class TextLocatorFactory {

  static Map<String, Function<String, ? extends AlexandriaTextLocator>> prefix2locator = new HashMap<>();

  static {
    prefix2locator.put(ByIdTextLocator.PREFIX, (string) -> new ByIdTextLocator().withId(string));
    prefix2locator.put(ByOffsetTextLocator.PREFIX, (string) -> {
      String[] startAndLength = string.split(",");
      Long start = Long.valueOf(startAndLength[0]);
      Long length = Long.valueOf(startAndLength[1]);
      return new ByOffsetTextLocator(start, length);
    });
  }

  private AlexandriaService service;

  @Inject
  public TextLocatorFactory(AlexandriaService service) {
    this.service = service;
  }

  public AlexandriaTextLocator fromString(String locatorString) throws TextLocatorParseException {
    String[] parts = locatorString.split(":", 2);
    String prefix = parts[0];
    if (prefix2locator.containsKey(prefix)) {
      return prefix2locator.get(prefix).apply(parts[1]);
    }
    throw new TextLocatorParseException("The locator prefix '" + prefix + "' is not a valid prefix. Valid prefixes: "
      + prefix2locator
      .keySet() + ".");
  }

  public void validate(AlexandriaTextLocator locator, AlexandriaResource resource) {
    InputStream textStream = service.getResourceTextAsStream(resource.getId())//
                                    .orElseThrow(() -> new BadRequestException("The resource has no text attached."));

    try {
      locator.validate(textStream);
    } catch (TextLocatorValidationException tlve) {
      throw new BadRequestException(tlve.getMessage());
    }
  }

}
