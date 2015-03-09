package nl.knaw.huygens.alexandria.helpers;

import static java.lang.String.format;

import javax.ws.rs.core.MediaType;
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

public class ApiFixture extends JerseyTest {
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

  private String body;

  private ClientResponse response;

  private String entity;

  @After
  public void resetInstanceFields() {
    body = null;
    response = null;
    entity = null;
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
    return invokeREST(method, path, body);
    /*
    // TODO: fold with method below, passing null as body (this is what happens inside Jersey anyway).
    // cleans up this duplication.
    response = client() //
        .resource(getBaseURI()) //
        .path(path) //
        .method(method, ClientResponse.class);

    System.err.println("HEADERS: " + response.getHeaders());
    responseBody = response.getEntity(String.class); // TODO: response.bufferEntity();
    return new MultiValueResult().with("status", status()).with("body", body());*/
  }

  protected MultiValueResult invokeREST(String method, String path, String body) {
    response = client() //
        .resource(getBaseURI()) //
        .path(path) //
        .type(MediaType.APPLICATION_JSON_TYPE) //
        .method(method, ClientResponse.class, this.body);
    entity = response.getEntity(String.class); // .replaceAll(hostInfo(), "{host}") ?

    System.err.println("HEADERS: " + response.getHeaders());
    System.err.println("ENTITY : " + entity);
    return new MultiValueResult().with("status", status()).with("body", response());
  }

  public void body(String body) {
    this.body = body;
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

  public Optional<JsonNode> json() {
    try {
      return Optional.ofNullable(new ObjectMapper().readTree(response()));
    } catch (IOException e) {
      return Optional.empty();
    }
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

}
