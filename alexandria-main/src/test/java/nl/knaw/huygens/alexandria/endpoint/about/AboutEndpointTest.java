package nl.knaw.huygens.alexandria.endpoint.about;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.MockedServiceEndpointTest;

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
    @SuppressWarnings("unchecked")
    Map<String, Object> map = response.readEntity(Map.class);
    Log.info("result={}", map);
    assertThat(map).containsKeys("version", "builddate", "started");
  }
}
