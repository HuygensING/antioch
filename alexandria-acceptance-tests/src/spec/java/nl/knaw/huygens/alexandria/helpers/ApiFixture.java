package nl.knaw.huygens.alexandria.helpers;

import static java.lang.String.format;
import static java.util.logging.Logger.getAnonymousLogger;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.squarespace.jersey2.guice.BootstrapUtils;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.config.JsonConfiguration;
import nl.knaw.huygens.alexandria.util.UUIDParser;
import org.concordion.api.extension.Extensions;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extensions(ApiExtension.class)
public class ApiFixture extends JerseyTest {
  private static final Logger LOG = LoggerFactory.getLogger(ApiFixture.class);

  private static final AlexandriaConfiguration CONFIG = testConfiguration();

  private static ResourceConfig resourceConfig;
  private WebTarget target;
  private Optional<MediaType> contentType;
  private Optional<String> optionalBody;
  private Response response;
  private String entity;

  private static AlexandriaConfiguration testConfiguration() {
    return () -> UriBuilder.fromUri("https://localhost/").port(4242).build();
  }

  public static void addClass(Class<?> resourceClass) {
//    resourceConfig.getClasses().add(resourceClass);
    resourceConfig.register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(JsonConfiguration.class);
      }
    });
  }

  public static <T> void addProviderForContext(Class<T> contextClass, T contextObject) {
    resourceConfig.register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(contextObject).to(contextClass);
      }
    });
  }

  public static void setupJerseyAndGuice(Module module) {
    LOG.trace("Setting up Jersey");
    resourceConfig = new ResourceConfig();
    LOG.trace("+- resourceConfig=[{}]", resourceConfig);

    resourceConfig.property(ServerProperties.TRACING, "ALL");
    resourceConfig.register(new LoggingFilter(getAnonymousLogger(), true));

    resourceConfig.packages("nl.knaw.huygens.alexandria.endpoint.resource");
    resourceConfig.register(JsonConfiguration.class);

    LOG.trace("Bootstrapping Jersey2-Guice bridge:");
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    LOG.trace("+- locator=[{}]", locator);

    final List<Module> modules = Arrays.asList(new ServletModule(), baseModule(), module);
    final Injector injector = BootstrapUtils.newInjector(locator, modules);
    LOG.trace("+- injector=[{}]", injector);

    BootstrapUtils.install(locator);
    LOG.trace("+- done: locator installed");
  }

  public String base() {
    return baseOf(location());
  }

  public String uuidQuality() {
    String idStr = tailOf(location());
    return UUIDParser.fromString(idStr).get().map(uuid -> "well-formed UUID").orElse("malformed UUID: " + idStr);
  }

  public void clear() {
    target = client().target(getBaseURI());
    contentType = Optional.empty();
    optionalBody = Optional.empty();
    response = null;
    entity = null;
  }

  public void request(String method, String path) {
    LOG.trace("request: method=[{}], path=[{}]", method, path);
    final Builder request = target.path(path).request(APPLICATION_JSON_TYPE);

    LOG.trace("ContentType=[{}], optionalBody=[{}]", contentType, optionalBody);
    response = optionalBody.map(body -> invokeWithEntity(request, method, body))
                           .orElse(invokeWithoutEntity(request, method));
    LOG.trace("response: [{}]", response);

    if (response.hasEntity()) {
      this.entity = response.readEntity(String.class).replaceAll(hostInfo(), "{host}");
    }
  }

  private Response invokeWithoutEntity(Builder request, String method) {
    return request.method(method, Response.class);
  }

  private Response invokeWithEntity(Builder request, String method, String body) {
    final Entity<String> entity = contentType.map(c -> Entity.entity(body, c)).orElse(Entity.json(body));
    return request.method(method, entity, Response.class);
  }

  public void body(String body) {
    this.optionalBody = Optional.of(body);
  }

  public void emptyBody() {
    this.optionalBody = Optional.empty();
  }

  public void contentType(String type) {
    this.contentType = Optional.of(MediaType.valueOf(type));
  }

  public String response() {
    return entity;
  }

  public String responseOrEmpty() {
    return Optional.ofNullable(Strings.emptyToNull(response())).orElse("empty");
  }

  public String headerContent(String name) {
    return header(name).map(content -> format("%s:%s", name, content)).orElse("(no name)");
  }

  public String location() {
    return responseLocation().map(l -> l.replaceAll(hostInfo(), "{host}")).orElse("no-location");
  }

  public String status() {
    final StatusType statusInfo = response.getStatusInfo();
    return format("%s %s", statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
  }

  @Override
  protected Application configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);
    return resourceConfig;
  }

  private static Module baseModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        LOG.trace("setting up Guice bindings");
        bind(AlexandriaConfiguration.class).toInstance(CONFIG);
        bind(AnnotationEntityBuilder.class).toInstance(AnnotationEntityBuilder.forConfig(CONFIG));
        bind(ResourceEntityBuilder.class).toInstance(ResourceEntityBuilder.forConfig(CONFIG));
      }
    };
  }

  protected URI getBaseURI() {
    return CONFIG.getBaseURI();
  }

  private Optional<String> header(String header) {
    return Optional.ofNullable(response.getHeaderString(header));
  }

  private String hostInfo() {
    final URI baseURI = getBaseURI();
    return format("%s:%d", baseURI.getHost(), baseURI.getPort());
  }

  private Optional<String> responseLocation() {
    return Optional.ofNullable(response.getLocation()).map(URI::toString);
  }

  private String baseOf(String s) {
    return s.substring(0, s.lastIndexOf('/') + 1);
  }

  private String tailOf(String s) {
    return Iterables.getLast(Splitter.on('/').split(s));
  }
}
