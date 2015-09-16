package nl.knaw.huygens.alexandria.endpoint.about;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.temporal.TemporalAmount;
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
    when(SERVICE_MOCK.getTentativesTimeToLive()).thenReturn(aTemporalAmount());

    Response response = target("about").request().get();
    Log.info("response={}", response);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(responseEntityAsMap(response))
        .containsKeys("version", "buildDate", "commitId", "startedAt", "scmBranch", "tentativesTTL");
  }

  private TemporalAmount aTemporalAmount() {
    return mock(TemporalAmount.class);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> responseEntityAsMap(Response response) {
    final Map result = response.readEntity(Map.class);
    Log.info("result={}", result);
    return result;
  }
}
