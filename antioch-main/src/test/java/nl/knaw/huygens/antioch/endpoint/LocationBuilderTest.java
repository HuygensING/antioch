package nl.knaw.huygens.antioch.endpoint;

/*
 * #%L
 * antioch-main
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

import java.net.URI;
import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.config.MockConfiguration;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.model.TentativeAntiochProvenance;

public class LocationBuilderTest {
  private final LocationBuilder lb = new LocationBuilder(new MockConfiguration(), new EndpointPathResolver());

  @Test
  public void testGetLocationOfAntiochAnnotationWithUUID() {
    UUID randomUUID = UUID.randomUUID();
    TentativeAntiochProvenance provenance = mock(TentativeAntiochProvenance.class);
    AntiochAnnotationBody body = mock(AntiochAnnotationBody.class);
    AntiochAnnotation annotation = new AntiochAnnotation(randomUUID, body, provenance);

    URI locationOf = lb.locationOf(annotation);

    assertThat(locationOf.toString()).isEqualTo("http://antioch.eg/annotations/" + randomUUID);
  }

  @Test
  public void testGetLocationOfDeprecatedAntiochAnnotation() {
    UUID randomUUID = UUID.randomUUID();
    String deprecatedId = randomUUID.toString() + ".0";

    URI locationOf = lb.locationOf(AntiochAnnotation.class, deprecatedId);

    assertThat(locationOf.toString()).isEqualTo("http://antioch.eg/annotations/" + randomUUID + "/" + EndpointPaths.REV + "/0");
  }

  @Test
  public void testGetLocationOfAntiochResourceWithUUID() {
    UUID randomUUID = UUID.randomUUID();
    TentativeAntiochProvenance provenance = mock(TentativeAntiochProvenance.class);
    AntiochResource resource = new AntiochResource(randomUUID, provenance);

    URI locationOf = lb.locationOf(resource);

    assertThat(locationOf.toString()).isEqualTo("http://antioch.eg/resources/" + randomUUID);
  }
}
