package nl.knaw.huygens.alexandria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.storage.Storage;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

public class TinkerpopAlexandriaServiceTest {
  private static final Storage mockStorage = mock(Storage.class);
  TinkerpopAlexandriaService service = new TinkerpopAlexandriaService(mockStorage);

  @Test
  public void test() {
    Graph g = TinkerGraph.open();
    Log.info("graph features: {}", g.features());
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.ofEpochSecond(1000000), "why");
    UUID uuid = new UUID(1l, 1l);
    String ref = "ref";
    boolean created = service.createOrUpdateResource(uuid, ref, provenance);
    assertThat(created).isTrue();
  }

}
