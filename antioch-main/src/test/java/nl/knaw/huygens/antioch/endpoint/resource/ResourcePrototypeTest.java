package nl.knaw.huygens.antioch.endpoint.resource;

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

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.antioch.jaxrs.ThreadContext;
import nl.knaw.huygens.antioch.model.AntiochProvenance;
import nl.knaw.huygens.antioch.test.AntiochTest;

public class ResourcePrototypeTest extends AntiochTest {

  private final ObjectMapper om = new ObjectMapper();

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
    assertThat(rp.getProvenance().get().getValue().getWhy()).isEqualTo(AntiochProvenance.DEFAULT_WHY);
  }
}
