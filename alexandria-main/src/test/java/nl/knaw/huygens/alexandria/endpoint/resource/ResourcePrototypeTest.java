package nl.knaw.huygens.alexandria.endpoint.resource;

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

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.jaxrs.ThreadContext;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class ResourcePrototypeTest extends AlexandriaTest {

  private ObjectMapper om = new ObjectMapper();

  @Test
  public void testJsonRepresentation() throws JsonProcessingException {
    ResourcePrototype rp = new ResourcePrototype();
    String json = om.writeValueAsString(rp);
    Log.info("json={}", json);
  }

  @Test
  public void testResourcePrototypeFromJson() throws IOException {
    UUID uuid = UUID.randomUUID();
    Instant instant = Instant.now();
    String json = singleQuotesToDouble("{"//
        + "'resource':{"//
        + "'id':'" + uuid.toString() + "',"//
        + "'ref':'whatever',"//
        + "'provenance':{"//
        + "'when':'" + instant + "'"//
        + "}"//
        + "}}");
    Log.info("json={}", json);
    ResourcePrototype rp = om.readValue(json, ResourcePrototype.class);
    Log.info("resourcePrototype={}", rp);
    assertThat(rp.getId().getValue()).isEqualTo(uuid);
    assertThat(rp.getRef()).isEqualTo("whatever");
    assertThat(rp.getProvenance().isPresent()).isTrue();
    assertThat(rp.getProvenance().get().getWhen()).isEqualTo(instant);
    assertThat(rp.getProvenance().get().getValue().getWho()).isEqualTo(ThreadContext.getUserName());
    assertThat(rp.getProvenance().get().getValue().getWhy()).isEqualTo(AlexandriaProvenance.DEFAULT_WHY);
  }
}
