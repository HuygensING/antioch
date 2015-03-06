package nl.knaw.huygens.alexandria.helpers;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.concordion.api.MultiValueResult;
import org.junit.After;
import org.junit.BeforeClass;

public class RESTJerseyTest extends JerseyTest {
  private static ResourceConfig resourceConfig;

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

  private ClientResponse response;

  private String responseBody;

  @After
  public void resetInstanceFields() {
    response = null;
    responseBody = null;
  }

  @Override
  protected AppDescriptor configure() {
    return new LowLevelAppDescriptor.Builder(resourceConfig).build();
  }

  @Override
  protected URI getBaseURI() {
    return UriBuilder.fromUri(super.getBaseURI()).scheme("https").build();
  }

  protected MultiValueResult invokeREST(String method, String path) {
    response = client() //
        .resource(getBaseURI()) //
        .path(path) //
        .method(method, ClientResponse.class);

    responseBody = response.getEntity(String.class);
    return new MultiValueResult().with("status", status()).with("body", body());
  }

  protected MultiValueResult invokeREST(String method, String path, String body) {
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

  protected Optional<String> location() {
    return header(HttpHeaders.LOCATION);
  }

  protected String body() {
    return responseBody; // cached because response.getEntity() consumes it.
  }

  protected Optional<JsonNode> json() {
    try {
      return Optional.ofNullable(new ObjectMapper().readTree(body()));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

}
