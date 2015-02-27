package nl.knaw.huygens.alexandria;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.StatusType;
import java.util.Optional;

import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.concordion.api.MultiValueResult;

public class RestFixture extends JerseyTest {
  public static final String RESOURCE_PKG = "nl.knaw.huygens.alexandria.resource";

  private static final String BASE_URI_FOR_TESTING = "https://localhost";

  private static final AppDescriptor LOW_LEVEL_APP_DESCRIPTOR =//
      new LowLevelAppDescriptor.Builder(RESOURCE_PKG) //
          .build();

  private ClientResponse response;

  private static final AppDescriptor GUICE_WEB_APP_DESCRIPTOR = //
      new WebAppDescriptor.Builder(RestFixture.RESOURCE_PKG) //
          .filterClass(GuiceFilter.class) //
          .contextPath("/") //
          .servletPath("/") //
          .clientConfig(new DefaultClientConfig()) //
          .build();

  public RestFixture() {
    super(LOW_LEVEL_APP_DESCRIPTOR);
    //super(GUICE_WEB_APP_DESCRIPTOR); // when we start needing Guice
  }

  public MultiValueResult invokeREST(String method, String path) {
    response = client() //
        .resource(BASE_URI_FOR_TESTING) //
        .path(path) //
        .method(method, ClientResponse.class);

    return new MultiValueResult().with("status", status()).with("body", body());
  }

  public MultiValueResult invokeREST(String method, String path, String body) {
    response = client() //
        .resource(BASE_URI_FOR_TESTING) //
        .path(path) //
        .method(method, ClientResponse.class, body);

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
    return response.getEntity(String.class);
  }

  protected Optional<String> location() {
    return header(HttpHeaders.LOCATION);
  }

}
