package nl.knaw.huygens.alexandria.storage;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AccountablePointer;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

public class TinkerpopAlexandriaServiceTest {
  private final TinkerPopService service = new TinkerGraphService();

  // @Test
  public void test() {
    Graph g = TinkerGraph.open();
    Log.info("graph features: {}", g.features());
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.ofEpochSecond(1000000), "why");
    UUID uuid = new UUID(1l, 1l);
    String ref = "ref";
    AlexandriaState state = AlexandriaState.CONFIRMED;
    boolean created = service.createOrUpdateResource(uuid, ref, provenance, state);
    assertThat(created).isTrue();
  }

  // @Test
  // TODO: fix test
  public void testDereferenceWithExistingAnnotation() {
    UUID id = UUID.randomUUID();
    AlexandriaAnnotationBody body = mock(AlexandriaAnnotationBody.class);
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    // when(mockStorage.readAnnotation(id)).thenReturn(Optional.of(new AlexandriaAnnotation(id, body, provenance)));
    AccountablePointer<AlexandriaAnnotation> ap = new AccountablePointer<>(AlexandriaAnnotation.class, id.toString());

    Optional<? extends Accountable> optional = service.dereference(ap);
    assertThat(optional.isPresent());
    AlexandriaAnnotation annotation = (AlexandriaAnnotation) optional.get();
    assertThat(annotation.getId()).isEqualTo(id);
  }

  @Test
  public void testDereferenceWithNonExistingAnnotation() {
    UUID id = UUID.randomUUID();
    // when(mockStorage.readAnnotation(id)).thenReturn(Optional.empty());
    AccountablePointer<AlexandriaAnnotation> ap = new AccountablePointer<>(AlexandriaAnnotation.class, id.toString());

    Optional<? extends Accountable> optional = service.dereference(ap);
    assertThat(optional.isPresent()).isFalse();
  }

  // @Test
  // TODO: fix test
  public void testDereferenceWithExistingResource() {
    UUID id = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    // when(mockStorage.readResource(id)).thenReturn(Optional.of(new AlexandriaResource(id, provenance)));
    AccountablePointer<AlexandriaResource> ap = new AccountablePointer<>(AlexandriaResource.class, id.toString());

    Optional<? extends Accountable> optional = service.dereference(ap);
    assertThat(optional.isPresent());
    AlexandriaResource annotation = (AlexandriaResource) optional.get();
    assertThat(annotation.getId()).isEqualTo(id);
  }
}
