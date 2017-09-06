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
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.config.AntiochConfiguration;
import nl.knaw.huygens.antioch.model.AntiochProvenance;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.model.TentativeAntiochProvenance;

import org.junit.Test;

public class ProvenanceEntityTest {

  @Test
  public void test_provenance_entity_creation() {
    UUID id = UUID.randomUUID();
    String who = "who";
    Instant when = Instant.now();
    String why = "why";
    String baseURI = "http://antioch.ax";

    TentativeAntiochProvenance tprovenance = new TentativeAntiochProvenance(who, when, why);
    AntiochResource resource = new AntiochResource(id, tprovenance);
    AntiochProvenance provenance = resource.getProvenance();
    AntiochConfiguration config = mock(AntiochConfiguration.class);
    when(config.getBaseURI()).thenReturn(URI.create(baseURI));
    LocationBuilder locationBuilder = new LocationBuilder(config, new EndpointPathResolver());
    ProvenanceEntity entity = ProvenanceEntity.of(provenance).withLocationBuilder(locationBuilder);

    String expectedLocation = baseURI + "/" + EndpointPaths.RESOURCES + "/" + id;
    URI what = entity.getWhat();
    Log.info("entity={}", entity);
    Log.info("what={}", what);
    assertThat(what.toString()).isEqualTo(expectedLocation);
    assertThat(entity.getWhen()).isEqualTo(when.toString());
    assertThat(entity.getWhy()).isEqualTo(why);
  }
}
