package nl.knaw.huygens.alexandria.textlocator;

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
    throw new TextLocatorParseException("locator prefix '" + prefix + "' not recognized");
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
          throw new BadRequestException("The text of the resource has no element with xml:id=\"" + id + "\"");
        }

      } catch (IOException | XPathExpressionException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
