package nl.knaw.huygens.alexandria.endpoint.provenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

import org.junit.Test;

public class ProvenanceEntityBuilderTest {
  @Test
  public void test_provenance_entity_creation_from_builder() {
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
    ProvenanceEntityBuilder builder = new ProvenanceEntityBuilder(config);
    ProvenanceEntity entity = builder.build(provenance);

    String expectedLocation = baseURI + "/" + EndpointPaths.RESOURCES + "/" + id;
    URI what = entity.getWhat();
    Log.info("entity={}", entity);
    Log.info("what={}", what);
    assertThat(what.toString()).isEqualTo(expectedLocation);
    assertThat(entity.getWhen()).isEqualTo(when);
    assertThat(entity.getWhy()).isEqualTo(why);
  }

}
