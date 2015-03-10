package nl.knaw.huygens.alexandria.helpers;

import static java.lang.String.format;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.concordion.api.extension.Extensions;
import org.junit.BeforeClass;

@Extensions(ApiExtension.class)
public class ApiFixture extends JerseyTest {
  private static ResourceConfig resourceConfig;
  private MediaType contentType;

  public static void addClass(Class<?> resourceClass) {
    resourceConfig.getClasses().add(resourceClass);
  }

  public static void addSingleton(Object resourceSingleton) {
    resourceConfig.getSingletons().add(resourceSingleton);
  }

  public static <T> void addProviderForContext(Class<T> contextClass, T contextObject) {
    addSingleton(new SingletonContextProvider<>(contextClass, contextObject));
  }

  @BeforeClass
  public static void resetStaticFields() {
    resourceConfig = new DefaultResourceConfig();
  }

  private WebResource request;

  private String body;

  private ClientResponse response;

  private String entity;

  @Override
  protected AppDescriptor configure() {
    return new LowLevelAppDescriptor.Builder(resourceConfig).build();
  }

  public void clear() {
    request = client().resource(getSecureBaseURI());
    contentType = MediaType.APPLICATION_JSON_TYPE;
    body = null;
    response = null;
    entity = null;
  }

  public void request(String method, String path) {
    System.err.println("*** ApiFixture.invokeREST: method=" + method + ", path=" + path);

    response = request.path(path).type(contentType).method(method, ClientResponse.class, body);

    entity = response.getEntity(String.class); // .replaceAll(hostInfo(), "{host}") ?
  }

  public void body(String body) {
    this.body = body;
  }

  public void contentType(String type) {
    this.contentType = MediaType.valueOf(type);
  }

  public String response() {
    return entity;
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

  protected URI getSecureBaseURI() {
    return UriBuilder.fromUri(super.getBaseURI()).scheme("https").build();
  }

  private Optional<String> header(String header) {
    return Optional.ofNullable(response.getHeaders().getFirst(header));
  }

  private String hostInfo() {
    final URI baseURI = getSecureBaseURI();
    return format("%s:%d", baseURI.getHost(), baseURI.getPort());
  }

  private Optional<String> responseLocation() {
    return Optional.ofNullable(response.getLocation()).map(URI::toString);
  }

}
