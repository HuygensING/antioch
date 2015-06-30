package nl.knaw.huygens.alexandria.concordion;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.Log;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.concordion.api.Command;
import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.listener.DocumentParsingListener;
import org.concordion.internal.ConcordionBuilder;
import org.reflections.Reflections;

public class RestExtension implements ConcordionExtension {
  public static final String REST_EXTENSION_NS = "http://alexandria.huygens.knaw.nl/concordion-extension";

  private final Set<Class<? extends Command>> commands = Sets.newHashSet();
  private final Map<String, String> htmlCommandTags = Maps.newHashMap();

  public RestExtension() {
    addAnnotatedCommands(new Reflections("nl.knaw.huygens.alexandria"));
  }

  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    registerCommands(concordionExtender);
    registerCommandToHtmlTranslator(concordionExtender);
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

  private void addAnnotatedCommands(Reflections scanner) {
    scanForAnnotatedClasses(scanner, HuygensCommand.class).forEach(this::addCommand);
  }

  private Set<Class<?>> scanForAnnotatedClasses(Reflections scanner, Class<? extends Annotation> annotationClass) {
    final Set<Class<?>> annotatedClasses = scanner.getTypesAnnotatedWith(annotationClass);

    if (Log.isDebugEnabled()) {
      final int annotatedClassesCount = annotatedClasses.size();
      final String annotationName = annotationClass.getSimpleName();
      final String classOrClasses = annotatedClassesCount == 1 ? "class" : "classes";
      Log.debug("Found {} @{} annotated {}", annotatedClassesCount, annotationName, classOrClasses);
    }

    return annotatedClasses;
  }

  @SuppressWarnings("unchecked")
  private void addCommand(Class<?> candidate) {
    if (Command.class.isAssignableFrom(candidate)) {
      Log.trace("Adding command: {}", candidate);
      commands.add((Class<? extends Command>) candidate);
    } else {
      Log.warn("Ignoring @{} class {} as it does not implement {}", //
          HuygensCommand.class.getSimpleName(), candidate.getName(), Command.class.getName());
    }
  }

  private void registerCommands(ConcordionExtender concordionExtender) {
    commands.stream().forEach(cmd -> {
      final HuygensCommand annotation = cmd.getAnnotation(HuygensCommand.class);
      final String name = annotation.name();
      final String tag = annotation.htmlTag();
      Log.trace("Command <{}> is translated to HTML tag <{}> and handled by {}", name, tag, cmd.getSimpleName());
      htmlCommandTags.put(name, tag);
      concordionExtender.withCommand(REST_EXTENSION_NS, name, instantiate(cmd));
    });
  }

  private Command instantiate(Class<? extends Command> cmd) {
    try {
      return cmd.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      Log.warn("Cannot instantiate command [{}]: {}", cmd.getName(), e);
      throw new RuntimeException(e);
    }
  }

  private void registerCommandToHtmlTranslator(ConcordionExtender concordionExtender) {
    concordionExtender.withDocumentParsingListener(new DocumentParsingListener() {
      @Override
      public void beforeParsing(Document document) {
        translate(document.getRootElement());
      }

      private void translate(Element element) {
        final Elements children = element.getChildElements();
        for (int i = 0; i < children.size(); i++) {
          translate(children.get(i));
        }

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
        return htmlCommandTags.getOrDefault(localName, localName);
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
