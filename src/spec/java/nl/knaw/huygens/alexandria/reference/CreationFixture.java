package nl.knaw.huygens.alexandria.reference;

import static org.concordion.api.MultiValueResult.multiValueResult;

import javax.ws.rs.core.Response.StatusType;
import java.util.Optional;
import java.util.UUID;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.concordion.api.MultiValueResult;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class CreationFixture extends JerseyTest {
  private static final String GENERATED_UUID = UUID.randomUUID().toString();//"0c29d08e-bc14-11e4-b9d7-4304ba0be2ce";

  @Override
  protected AppDescriptor configure() {
    return new LowLevelAppDescriptor.Builder("nl.knaw.huygens.alexandria.resource").build();
  }

  public String checkUUID(String str) {
    try {
      UUID.fromString(str);
      return "valid UUID";
    } catch (IllegalArgumentException e) {
      return str;
    }
  }

  public MultiValueResult rest(String method, String baseURI, String path, String body) {
    final ClientResponse blaat = client() //
        .resource(baseURI) //
        .path(path) //
        .method(method, ClientResponse.class, body);

    final StatusType statusInfo = blaat.getStatusInfo();

    final String location = Optional.ofNullable(blaat.getHeaders().getFirst("Location")).orElse("/");
    return multiValueResult() //
        .with("status", statusInfo.getStatusCode() + " " + statusInfo.getReasonPhrase()) //
        .with("locationStart", location.substring(0, location.lastIndexOf('/') + 1)) //
        .with("locationId", checkUUID(location.substring(location.lastIndexOf('/') + 1))) //
        .with("body", blaat.getEntity(String.class));
  }
}
