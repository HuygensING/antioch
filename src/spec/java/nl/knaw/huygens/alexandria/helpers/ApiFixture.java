package nl.knaw.huygens.alexandria.helpers;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.util.ObjectMapperProvider;
import nl.knaw.huygens.alexandria.util.UUIDParser;
import org.concordion.api.extension.Extensions;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extensions(ApiExtension.class)
public class ApiFixture extends JerseyTest {
  private static final Logger LOG = LoggerFactory.getLogger(ApiFixture.class);

  private static final AlexandriaConfiguration CONFIG = testConfiguration();

  private static ResourceConfig resourceConfig;
  private WebTarget target;
  private MediaType contentType;
  private String body;
  private ClientResponse response;
  private String entity;

  private static AlexandriaConfiguration testConfiguration() {
    return () -> UriBuilder.fromUri("https://localhost/").port(4242).build();
  }

  public static void addClass(Class<?> resourceClass) {
//    resourceConfig.getClasses().add(resourceClass);
    resourceConfig.register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(ObjectMapperProvider.class);
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

  @BeforeClass
  public static void resetStaticFields() {
    LOG.trace("resetting Jersey Config");
    resourceConfig = new ResourceConfig();

    LOG.trace("adding AlexandriaConfigurationProvider");
    addProviderForContext(AlexandriaConfiguration.class, CONFIG);
    addProviderForContext(AnnotationEntityBuilder.class, AnnotationEntityBuilder.forConfig(CONFIG));
    addProviderForContext(ResourceEntityBuilder.class, ResourceEntityBuilder.forConfig(CONFIG));

    LOG.trace("adding ObjectMapperProvider");
    addClass(ObjectMapperProvider.class);
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
    contentType = MediaType.APPLICATION_JSON_TYPE;
    body = null;
    response = null;
    entity = null;
  }

  public void request(String method, String path) {
    LOG.trace("request: method=[{}], path=[{}]", method, path);

    response = target.path(path).request().method(method, entity(body, contentType), ClientResponse.class);

    if (response.hasEntity()) {
      this.entity = response.readEntity(String.class).replaceAll(hostInfo(), "{host}");
    }
  }

  public void body(String body) {
    this.body = body;
  }

  public void emptyBody() {
    this.body = null;
  }

  public void contentType(String type) {
    this.contentType = MediaType.valueOf(type);
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
    return resourceConfig;
  }

  protected URI getBaseURI() {
    return CONFIG.getBaseURI();
  }

  private Optional<String> header(String header) {
    return Optional.ofNullable(response.getHeaders().getFirst(header));
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
