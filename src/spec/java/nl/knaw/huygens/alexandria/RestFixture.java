package nl.knaw.huygens.alexandria;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.concordion.api.MultiValueResult;
import org.junit.After;

public class RestFixture extends JerseyTest {
  private static ResourceConfig resourceConfig = new DefaultResourceConfig();

  private ClientResponse response;
  private String responseBody;

  public static void addClass(Class<?> resourceClass) {
    resourceConfig.getClasses().add(resourceClass);
  }

  public static void addSingleton(Object resourceSingleton) {
    resourceConfig.getSingletons().add(resourceSingleton);
  }

  public static <T> void addProviderForContext(Class<T> contextClass, T contextObject) {
    addSingleton(new SingletonContextProvider<>(contextClass, contextObject));
  }

  @Override
  protected AppDescriptor configure() {
    return new LowLevelAppDescriptor.Builder(resourceConfig).build();
  }

  @Override
  protected URI getBaseURI() {
    return UriBuilder.fromUri(super.getBaseURI()).scheme("https").build();
  }

  public MultiValueResult invokeREST(String method, String path) {
    response = client() //
        .resource(getBaseURI()) //
        .path(path) //
        .method(method, ClientResponse.class);

    responseBody = response.getEntity(String.class);
    return new MultiValueResult().with("status", status()).with("body", body());
  }

  public MultiValueResult invokeREST(String method, String path, String body) {
    response = client() //
        .resource(getBaseURI()) //
        .path(path) //
        .method(method, ClientResponse.class, body);

    responseBody = response.getEntity(String.class);
    return new MultiValueResult().with("status", status()).with("body", body());
  }

  protected String status() {
    final StatusType statusInfo = response.getStatusInfo();
    return String.format("%s %s", statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
  }

  protected Optional<String> header(String name) {
    return Optional.ofNullable(response.getHeaders().getFirst(name));
  }

  protected String body() {
    return responseBody; // cached because response.getEntity() consumes it.
  }

  @After
  public void resetFixture() {
    System.err.println("resetFixture");
    response = null;
    responseBody = null;
  }

  protected Optional<String> location() {
    return header(HttpHeaders.LOCATION);
  }

}
