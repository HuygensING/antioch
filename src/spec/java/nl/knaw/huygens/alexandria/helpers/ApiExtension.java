package nl.knaw.huygens.alexandria.helpers;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.listener.DocumentParsingListener;
import org.concordion.internal.listener.AssertResultRenderer;

public class ApiExtension implements ConcordionExtension {
  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    registerIncludesJsonCommand(concordionExtender);
    registerLinkedCSS(concordionExtender);
    addHeader(concordionExtender);
  }

  private void registerIncludesJsonCommand(ConcordionExtender concordionExtender) {
    IncludesJsonCommand includesJson = new IncludesJsonCommand();
    includesJson.addAssertEqualsListener(new AssertResultRenderer());
    concordionExtender.withCommand("https://alexandria.huygens.knaw.nl", "includesJson", includesJson);
  }

  private void registerLinkedCSS(ConcordionExtender concordionExtender) {
    concordionExtender.withLinkedCSS("/concordion.css", new Resource("/concordion.css"));
  }

  private void addHeader(ConcordionExtender concordionExtender) {
    concordionExtender.withDocumentParsingListener(new DocumentParsingListener() {
      @Override
      public void beforeParsing(Document document) {
        Element header = new Element("div");
        header.addAttribute(new Attribute("class", "header"));
        header.appendChild(title("Alexandria Text Annotation Repository"));
        header.appendChild(subtitle("REST API Specification"));
        Element body = (Element) document.query("//body").get(0);
        body.insertChild(header, 0);
      }

      private Element title(String value) {
        Element title = new Element("h1");
        title.appendChild(value);
        return title;
      }

      private Element subtitle(String value) {
        Element title = new Element("p");
        title.appendChild(value);
        return title;
      }
    });
  }
}
