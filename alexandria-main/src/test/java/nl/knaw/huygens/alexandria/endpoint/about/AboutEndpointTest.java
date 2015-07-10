package nl.knaw.huygens.alexandria.endpoint.about;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.MockedServiceEndpointTest;
import org.junit.BeforeClass;
import org.junit.Test;

public class AboutEndpointTest extends MockedServiceEndpointTest {
  @BeforeClass
  public static void registerEndpoint() {
    register(AboutEndpoint.class);
  }

  @Test
  public void test() {
    Response response = target("about").request().get();
    Log.info("response={}", response);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(responseEntityAsMap(response)).containsKeys("version", "buildDate", "commitId", "startedAt");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> responseEntityAsMap(Response response) {
    final Map result = response.readEntity(Map.class);
    Log.info("result={}", result);
    return result;
  }
}
