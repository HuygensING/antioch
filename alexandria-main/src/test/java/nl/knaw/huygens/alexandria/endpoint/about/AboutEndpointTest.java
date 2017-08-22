package nl.knaw.huygens.alexandria.endpoint.about;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.temporal.TemporalAmount;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.MockedServiceEndpointTest;

public class AboutEndpointTest extends MockedServiceEndpointTest {
  @BeforeClass
  public static void registerEndpoint() {
    register(AboutEndpoint.class);
  }

  @Test
  public void test() {
    when(SERVICE_MOCK.getTentativesTimeToLive()).thenReturn(aTemporalAmount());

    Response response = target(EndpointPaths.ABOUT).request().get();
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
    final Map<String, Object> result = response.readEntity(Map.class);
    Log.info("result={}", result);
    return result;
  }
}
