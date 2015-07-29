package nl.knaw.huygens.alexandria.storage;

import static org.assertj.core.api.StrictAssertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

public class StorageTest {

  Storage storage = new TinkerGraphStorage();

  @Test
  public void testReadAfterCreateIsIdentity() {
    UUID id = UUID.randomUUID();
    String who = "who";
    Instant when = Instant.now();
    String why = "why";
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(who, when, why);
    AlexandriaResource resource = new AlexandriaResource(id, provenance);
    resource.setCargo("reference");
    // logGraphAsJson();
    Log.info("resource={}", resource);
    storage.createOrUpdateResource(resource);
    // logGraphAsJson();
    Log.info("after createOrUpdate");
    AlexandriaResource read = storage.readResource(id).get();
    Log.info("after read");

    // logGraphAsJson();
    Log.info("read={}", read);
    assertThat(read).isEqualToComparingOnlyGivenFields(resource, "cargo", "id", "state");
    assertThat(read.getProvenance()).isEqualToComparingOnlyGivenFields(resource.getProvenance(), "who", "why", "when");
  }

  // private void logGraphAsJson() {
  // try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
  // g.io(new GraphSONIo.Builder()).writer().create().writeGraph(os, g);
  // Log.info("graph as json={}", new String(os.toByteArray(), StandardCharsets.UTF_8));
  // } catch (IOException e) {
  // throw new RuntimeException(e);
  // }
  // }
}
