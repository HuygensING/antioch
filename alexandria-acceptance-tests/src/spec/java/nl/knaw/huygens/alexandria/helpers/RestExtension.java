package nl.knaw.huygens.alexandria.helpers;

import java.util.HashMap;
import java.util.Map;

import nl.knaw.huygens.Log;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.listener.DocumentParsingListener;
import org.concordion.internal.ConcordionBuilder;

public class RestExtension implements ConcordionExtension {
  public static final String REST_EXTENSION_NS = "http://alexandria.huygens.knaw.nl/concordion-extension";

  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    registerCommands(concordionExtender);
    registerCommandDecorator(concordionExtender);
    registerLinkedCSS(concordionExtender);
    addHeader(concordionExtender);

    /* HACK to make the fixture (the instance of the JerseyTest) available in our Concordion Commands.
     *
     * Using the force / reading the source, we know that the incoming ConcordionExtender at this point is
     * actually a ConcordionBuilder. We can trick this builder into accepting a new EvaluatorFactory, and as
     * the fixture instance is passed through createEvaluator, we can intercept it and store it in an Evaluator
     * of our own where we can make the fixture available at a later time.
     */
    ((ConcordionBuilder) concordionExtender)
        .withEvaluatorFactory(fixture -> new FixtureEvaluator((RestFixture) fixture));
  }

  private void registerCommands(ConcordionExtender concordionExtender) {
    concordionExtender.withCommand(REST_EXTENSION_NS, "request", new RequestCommand());
    concordionExtender.withCommand(REST_EXTENSION_NS, "get", new HttpMethodCommand("GET"));
    concordionExtender.withCommand(REST_EXTENSION_NS, "post", new HttpMethodCommand("POST"));
    concordionExtender.withCommand(REST_EXTENSION_NS, "put", new HttpMethodCommand("PUT"));
    concordionExtender.withCommand(REST_EXTENSION_NS, "delete", new HttpMethodCommand("DELETE"));
    concordionExtender.withCommand(REST_EXTENSION_NS, "jsonBody", new JsonBodyCommand());
    concordionExtender.withCommand(REST_EXTENSION_NS, "status", new ExpectedStatusCommand());
    concordionExtender.withCommand(REST_EXTENSION_NS, "header", new ExpectedHeaderCommand());
    concordionExtender.withCommand(REST_EXTENSION_NS, "location", new ExpectedLocationCommand());
    concordionExtender.withCommand(REST_EXTENSION_NS, "jsonResponse", new ExpectedJsonResponseCommand());
  }

  private void registerCommandDecorator(ConcordionExtender concordionExtender) {
    concordionExtender.withDocumentParsingListener(new DocumentParsingListener() {
      private Map<String, String> tags = new HashMap<String, String>() {{
        put("request", "div");
        put("get", "code");
        put("post", "code");
        put("put", "code");
        put("delete", "code");
        put("jsonBody", "pre");
        put("status", "code");
        put("header", "code");
        put("location", "code");
        put("jsonResponse", "pre");
      }};

      @Override
      public void beforeParsing(Document document) {
        visit(document.getRootElement());
      }

      private void visit(Element element) {
        final Elements children = element.getChildElements();
        for (int i = 0; i < children.size(); i++) {
          visit(children.get(i));
        }

        Log.trace("checking element: [{}]", element.getLocalName());
        if (REST_EXTENSION_NS.equals(element.getNamespaceURI())) {
          Attribute attr = new Attribute(element.getLocalName(), "");
          attr.setNamespace("h", REST_EXTENSION_NS);
          element.addAttribute(attr);
          element.setNamespacePrefix("");
          element.setNamespaceURI(null);
          element.setLocalName(translate(element.getLocalName()));
        }
      }

      private String translate(String localName) {
        final String translated = tags.getOrDefault(localName, localName);
        Log.trace("translating: [{}] -> [{}]", localName, translated);
        return translated;
      }
    });
  }

  private void registerLinkedCSS(ConcordionExtender concordionExtender) {
    concordionExtender.withLinkedCSS("/concordion.css", new Resource("/concordion.css"));
  }

  private void addHeader(ConcordionExtender concordionExtender) {
    concordionExtender.withDocumentParsingListener(new DocumentParsingListener() {
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

      @Override
      public void beforeParsing(Document document) {
        Element header = new Element("div");
        header.addAttribute(new Attribute("class", "page-header"));
        header.appendChild(title("Alexandria Text Annotation Repository"));
        header.appendChild(subtitle("REST API Specification"));
        Element body = (Element) document.query("//body").get(0);
        body.insertChild(header, 0);
      }

    });
  }
}
