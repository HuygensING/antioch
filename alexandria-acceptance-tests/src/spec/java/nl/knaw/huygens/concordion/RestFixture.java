package nl.knaw.huygens.concordion;

import static java.lang.String.format;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.squarespace.jersey2.guice.BootstrapUtils;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.service.TinkerpopAlexandriaService;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.util.UUIDParser;
import org.concordion.api.extension.Extensions;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;

@Extensions(RestExtension.class)
public class RestFixture extends JerseyTest {

  private static final AlexandriaConfiguration CONFIG = testConfiguration();

  private static TinkerpopAlexandriaService service = new TinkerpopAlexandriaService(new Storage());

  private static boolean jersey2GuiceBridgeInitialised;

  private static ResourceConfig application;

  private WebTarget target;
  private Optional<MediaType> optionalContentType;
  private Optional<String> optionalBody;
  private Response response;
  private Optional<String> entity;
  private String method;
  private String url;
  private final Map<String, String> headers = new HashMap<>();

  @BeforeClass
  public static void setup() {
    Log.debug("Setting up Jersey");

    application = new AcceptanceTestApplication();
    Log.trace("+- application=[{}]", application);

    if (!jersey2GuiceBridgeInitialised) {
      initialiseJersey2GuiceBridge();
      jersey2GuiceBridgeInitialised = true;
    }
  }

  protected static void register(Class<?> componentClass) {
    application.register(componentClass);
  }

  private static AlexandriaConfiguration testConfiguration() {
    return new AlexandriaConfiguration() {
      @Override
      public URI getBaseURI() {
        return UriBuilder.fromUri("https://localhost/").port(4242).build();
      }

      @Override
      public String getStorageDirectory() {
        return "/tmp";
      }

      @Override
      public String toString() {
        return Objects.toStringHelper(this).add("baseURI", getBaseURI()).toString();
      }
    };
  }

  private static void initialiseJersey2GuiceBridge() {
    Log.debug("Bootstrapping Jersey2-Guice bridge");

    final ServiceLocator locator = BootstrapUtils.newServiceLocator();
    Log.trace("+- locator=[{}]", locator);

    final Injector injector = BootstrapUtils.newInjector(locator, Arrays.asList(baseModule()));
    Log.trace("+- injector=[{}]", injector);

    BootstrapUtils.install(locator);
    Log.trace("+- done: locator installed");
  }

  private static Module baseModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        Log.trace("setting up Guice bindings");
        bind(AlexandriaService.class).toInstance(service);
        bind(AlexandriaConfiguration.class).toInstance(CONFIG);
        bind(AnnotationEntityBuilder.class).in(Scopes.SINGLETON);
        bind(EndpointPathResolver.class).in(Scopes.SINGLETON);
        bind(ResourceEntityBuilder.class).in(Scopes.SINGLETON);
      }
    };
  }

  public void clear() {
    Log.debug("Clearing {}", getClass().getSimpleName());

    target = client().target(getBaseUri());
    Log.trace("+- refreshed WebTarget: [{}]", target);

    optionalContentType = Optional.empty();
    optionalBody = Optional.empty();
    response = null;
    entity = Optional.empty();
    headers.clear();
    Log.trace("+- done (request details cleared)");
  }

  public void clearStorage() {
    Log.debug("Clearing Storage");
    service.setStorage(new Storage());
  }

  public RestFixture method(String method) {
    Log.trace("method set to: [{}]", method);
    this.method = method;
    return this;
  }

  public RestFixture url(String url) {
    Log.trace("url set to: [{}]", url);
    this.url = url;
    return this;
  }

  public RestFixture execute() {
    Log.trace("executing");
    request(method, url);
    return this;
  }

  public void request(String method, String path) {
    Log.trace("request: method=[{}], path=[{}]", method, path);
    Builder invoker = target.path(path).request(APPLICATION_JSON_TYPE);

    for (Map.Entry<String, String> entry : headers.entrySet()) {
      Log.trace("header: {}: {}", entry.getKey(), entry.getValue());
      invoker = invoker.header(entry.getKey(), entry.getValue());
    }

    if (optionalBody.isPresent()) {
      final MediaType mediaType = optionalContentType.orElse(APPLICATION_JSON_TYPE);
      response = invoker.method(method, Entity.entity(optionalBody.get(), mediaType), Response.class);
    } else {
      response = invoker.method(method, Response.class);
    }
    Log.trace("response: [{}]", response);

    if (response == null) {
      throw new IllegalStateException("Invoker yielded null Response");
    }

    if (response.hasEntity()) {
      final String rawEntity = response.readEntity(String.class);
      entity = Optional.of(normalizeHostInfo(rawEntity));
      Log.trace("read response entity: [{}]", entity);
    }
  }

  public void body(String body) {
    optionalBody = Optional.of(body);
  }

  public void emptyBody() {
    body("");
  }

  public void setHeader(String name, String value) {
    headers.put(name, value);
  }

  public void contentType(String type) {
    optionalContentType = Optional.of(MediaType.valueOf(type));
  }

  public Optional<String> response() {
    return entity; //.orElse("empty");
  }

  public Optional<String> location() {
    return Optional.ofNullable(response.getLocation()).map(URI::toString).map(this::normalizeHostInfo);
  }

  public Optional<String> header(String header) {
    return Optional.ofNullable(response.getHeaderString(header));
  }

  public String status() {
    final StatusType statusInfo = response.getStatusInfo();
    return format("%s %s", statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
  }

  @Override
  protected Application configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);
    return application;
  }

  @Override
  protected URI getBaseUri() {
    return CONFIG.getBaseURI();
  }

  protected AlexandriaService service() {
    return service;
  }

  private Optional<UUID> parse(String idStr) {
    return UUIDParser.fromString(idStr).get();
  }

  private Supplier<String> malformedDescription(String idStr) {
    return () -> "malformed UUID: " + idStr;
  }

  private String normalizeHostInfo(String s) {
    return s.replaceAll(hostInfo(), "{host}");
  }

  private String hostInfo() {
    final URI baseURI = getBaseUri();
    return format("%s:%d", baseURI.getHost(), baseURI.getPort());
  }

  private String baseOf(String s) {
    return s.substring(0, s.lastIndexOf('/') + 1);
  }

  private String tailOf(String s) {
    return Iterables.getLast(Splitter.on('/').split(s));
  }
}
