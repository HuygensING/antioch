package nl.knaw.huygens.alexandria.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONIo;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

public class StorageTest {

  Graph g = TinkerGraph.open();
  Storage storage = new Storage(g);

  @Test
  public void testReadAfterCreateIsIdentity() {
    UUID id = UUID.randomUUID();
    String who = "who";
    Instant when = Instant.now();
    String why = "why";
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(who, when, why);
    AlexandriaResource resource = new AlexandriaResource(id, provenance);
    resource.setRef("reference");
    logGraphAsJson();
    Log.info("resource={}", resource);
    storage.createOrUpdateResource(resource);
    logGraphAsJson();
    Log.info("after createOrUpdate");
    AlexandriaResource read = storage.readResource(id);
    Log.info("after read");

    logGraphAsJson();
    Log.info("read={}", read);
    assertThat(read).isEqualToComparingOnlyGivenFields(resource, "ref", "id", "state");
    assertThat(read.getProvenance()).isEqualToComparingOnlyGivenFields(resource.getProvenance(), "who", "why", "when");
  }

  private void logGraphAsJson() {
    try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      g.io(new GraphSONIo.Builder()).writer().create().writeGraph(os, g);
      Log.info("graph as json={}", new String(os.toByteArray(), StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  // @Test
  // public void test() {
  // String string = ResourceVF.class.getPackage().getName() + ".";
  // Log.info("string={}", string);
  // Set<Class<?>> framerClasses = new Reflections(string).getTypesAnnotatedWith(Framer.class);
  // Log.info("classes={}", framerClasses);
  // }
}
