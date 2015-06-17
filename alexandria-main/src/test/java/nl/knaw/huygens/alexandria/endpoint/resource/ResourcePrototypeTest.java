package nl.knaw.huygens.alexandria.endpoint.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResourcePrototypeTest {

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
    String json = ("{"//
        + "'resource':{"//
        + "'id':'" + uuid.toString() + "',"//
        + "'ref':'whatever',"//
        + "'provenance':{"//
        + "'when':'" + instant + "'"//
        + "}"//
    + "}}").replace("'", "\"");
    Log.info("json={}", json);
    ResourcePrototype rp = om.readValue(json, ResourcePrototype.class);
    Log.info("resourcePrototype={}", rp);
    assertThat(rp.getId().getValue()).isEqualTo(uuid);
    assertThat(rp.getRef()).isEqualTo("whatever");
    assertThat(rp.getProvenance().isPresent()).isTrue();
    assertThat(rp.getProvenance().get().getWhen()).isEqualTo(instant);
    assertThat(rp.getProvenance().get().getValue().getWho()).isEqualTo(AlexandriaProvenance.DEFAULT_WHO);
    assertThat(rp.getProvenance().get().getValue().getWhy()).isEqualTo(AlexandriaProvenance.DEFAULT_WHY);
  }
}
