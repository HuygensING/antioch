package nl.knaw.huygens.alexandria.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaResourceVF;

import org.junit.Test;
import org.reflections.Reflections;

import peapod.internal.runtime.Framer;

import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

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
    logGraphAsJson();
    Log.info("resource={}", resource);
    storage.createOrUpdate(resource);
    logGraphAsJson();
    Log.info("after createOrUpdate");
    AlexandriaResource read = storage.read(AlexandriaResource.class, id);
    Log.info("after read");

    logGraphAsJson();
    Log.info("read={}", read);
    assertThat(read).isEqualToComparingOnlyGivenFields(resource, "ref", "id", "state");
    assertThat(read.getProvenance()).isEqualToComparingOnlyGivenFields(resource.getProvenance(), "who", "why", "when");
  }

  private void logGraphAsJson() {
    try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      g.io().graphSONWriter().create().writeGraph(os, g);
      Log.info("graph as json={}", new String(os.toByteArray(), StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void test() {
    String string = AlexandriaResourceVF.class.getPackage().getName() + ".";
    Log.info("XXstring={}", string);
    Set<Class<?>> framerClasses = new Reflections(string).getTypesAnnotatedWith(Framer.class);
    Log.info("XXclasses={}", framerClasses);
  }
}
