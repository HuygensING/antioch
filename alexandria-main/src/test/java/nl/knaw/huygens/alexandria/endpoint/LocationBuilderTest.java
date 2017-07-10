package nl.knaw.huygens.alexandria.endpoint;

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

import java.net.URI;
import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

public class LocationBuilderTest {
  LocationBuilder lb = new LocationBuilder(new MockConfiguration(), new EndpointPathResolver());

  @Test
  public void testGetLocationOfAlexandriaAnnotationWithUUID() {
    UUID randomUUID = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = mock(TentativeAlexandriaProvenance.class);
    AlexandriaAnnotationBody body = mock(AlexandriaAnnotationBody.class);
    AlexandriaAnnotation annotation = new AlexandriaAnnotation(randomUUID, body, provenance);

    URI locationOf = lb.locationOf(annotation);

    assertThat(locationOf.toString()).isEqualTo("http://alexandria.eg/annotations/" + randomUUID);
  }

  @Test
  public void testGetLocationOfDeprecatedAlexandriaAnnotation() {
    UUID randomUUID = UUID.randomUUID();
    String deprecatedId = randomUUID.toString() + ".0";

    URI locationOf = lb.locationOf(AlexandriaAnnotation.class, deprecatedId);

    assertThat(locationOf.toString()).isEqualTo("http://alexandria.eg/annotations/" + randomUUID + "/" + EndpointPaths.REV + "/0");
  }

  @Test
  public void testGetLocationOfAlexandriaResourceWithUUID() {
    UUID randomUUID = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = mock(TentativeAlexandriaProvenance.class);
    AlexandriaResource resource = new AlexandriaResource(randomUUID, provenance);

    URI locationOf = lb.locationOf(resource);

    assertThat(locationOf.toString()).isEqualTo("http://alexandria.eg/resources/" + randomUUID);
  }
}
