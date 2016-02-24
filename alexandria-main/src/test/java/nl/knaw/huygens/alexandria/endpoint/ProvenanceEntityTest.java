package nl.knaw.huygens.alexandria.endpoint;

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

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

import org.junit.Test;

public class ProvenanceEntityTest {

  @Test
  public void test_provenance_entity_creation() {
    UUID id = UUID.randomUUID();
    String who = "who";
    Instant when = Instant.now();
    String why = "why";
    String baseURI = "http://alexandria.ax";

    TentativeAlexandriaProvenance tprovenance = new TentativeAlexandriaProvenance(who, when, why);
    AlexandriaResource resource = new AlexandriaResource(id, tprovenance);
    AlexandriaProvenance provenance = resource.getProvenance();
    AlexandriaConfiguration config = mock(AlexandriaConfiguration.class);
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
