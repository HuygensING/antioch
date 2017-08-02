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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

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
    prefix2locator.put(ByXPathTextLocator.PREFIX, (string) -> new ByXPathTextLocator().withXPath(string));
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
    // InputStream textStream = service.getResourceTextAsStream(resource.getId())//
    // .orElseThrow(() -> new BadRequestException("The resource has no text attached."));
    //
    // try {
    // locator.validate(textStream);
    // } catch (TextLocatorValidationException tlve) {
    // throw new BadRequestException(tlve.getMessage());
    // }
  }

}
