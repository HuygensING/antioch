package nl.knaw.huygens.alexandria.endpoint.about;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
