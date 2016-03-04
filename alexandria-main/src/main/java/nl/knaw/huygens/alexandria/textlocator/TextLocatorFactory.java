package nl.knaw.huygens.alexandria.textlocator;

import java.io.InputStream;

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

  public AlexandriaTextLocator fromString(String locatorString) throws TextLocatorParseException {
    String[] parts = locatorString.split(":", 2);
    String prefix = parts[0];
    if (ByIdTextLocator.PREFIX.equals(prefix)) {
      return new ByIdTextLocator().withId(parts[1]);
    } else if (ByOffsetTextLocator.PREFIX.equals(prefix)) {
      String[] startAndLength = parts[1].split(",");
      Long start = Long.valueOf(startAndLength[0]);
      Long length = Long.valueOf(startAndLength[1]);
      return new ByOffsetTextLocator(start, length);
    }
    throw new TextLocatorParseException("The locator prefix '" + prefix + "' is not a valid prefix. Valid prefix: 'id'.");
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
