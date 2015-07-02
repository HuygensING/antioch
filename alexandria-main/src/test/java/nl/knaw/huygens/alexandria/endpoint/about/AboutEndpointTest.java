package nl.knaw.huygens.alexandria.endpoint.about;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;

import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.about.AboutEndpoint;

public class AboutEndpointTest extends JerseyTest {
  @Override
  protected Application configure() {
    return new ResourceConfig(AboutEndpoint.class);
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
