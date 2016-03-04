package nl.knaw.huygens.alexandria.textlocator;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class TextLocatorFactory {

  private AlexandriaService service;

  @Inject
  public TextLocatorFactory(AlexandriaService service) {
    this.service = service;
  }

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

  public AlexandriaTextLocator fromString(String locatorString) throws TextLocatorParseException {
    String[] parts = locatorString.split(":", 2);
    String prefix = parts[0];
    if (prefix2locator.containsKey(prefix)) {
      return prefix2locator.get(prefix).apply(parts[1]);
    }
    throw new TextLocatorParseException("The locator prefix '" + prefix + "' is not a valid prefix. Valid prefixes: " + prefix2locator.keySet() + ".");
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
