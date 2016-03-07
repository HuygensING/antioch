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

//  public void validate(AlexandriaTextLocator locator, AlexandriaResource resource) {
//    Optional<InputStream> textStream = service.getResourceTextAsStream(resource.getId());//
//    try {
//      String xml = IOUtils.toString(textStream.get());
//      QueryableDocument qDocument = QueryableDocument.createFromXml(xml, true);
//      if (locator instanceof ByIdTextLocator) {
//        ByIdTextLocator byId = (ByIdTextLocator) locator;
//        String id = byId.getId();
//        Boolean idExists = qDocument.evaluateXPathToBoolean("boolean(//*[@xml:id=\"" + id + "\"])");
//        if (!idExists) {
//          throw new BadRequestException("The resource text has no element with xml:id=\"" + id + "\"");
//        }
//
//      } else if (locator instanceof ByXPathTextLocator) {
//        ByXPathTextLocator byId = (ByXPathTextLocator) locator;
//        String xpath = byId.getXPath();
//        String result = qDocument.evaluateXPathToString(xpath);
//        // if (StringUtils.isEmpty(result)) {
//        // throw new BadRequestException("The xpath " + xpath + "is not valid for the resource text.");
//        // }
//      }
//    } catch (IOException | XPathExpressionException e) {
//      throw new RuntimeException(e);
//    }

  }
}
