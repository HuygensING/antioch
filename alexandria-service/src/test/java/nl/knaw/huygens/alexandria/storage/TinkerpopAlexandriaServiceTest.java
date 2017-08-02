package nl.knaw.huygens.alexandria.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/*
 * #%L
 * alexandria-service
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

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.IdentifiablePointer;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.TinkerPopService;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;

public class TinkerpopAlexandriaServiceTest {
  private static final Storage mockStorage = mock(Storage.class);
  private final TinkerPopService service = new TinkerPopService(mockStorage, new LocationBuilder(new MockConfiguration(), new EndpointPathResolver()));

  @SuppressWarnings("unchecked")
  @Before
  public void before() {
    when(mockStorage.runInTransaction(Mockito.any(Supplier.class))).thenCallRealMethod();//
    when(mockStorage.getTransactionIsOpen()).thenCallRealMethod();
    doCallRealMethod().when(mockStorage).setTransactionIsOpen(Mockito.any(Boolean.class));
  }

  @Ignore
  @Test
  public void test() {
    Graph g = TinkerGraph.open();
    Log.info("graph features: {}", g.features());
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.ofEpochSecond(1000000), "why");
    UUID uuid = new UUID(1L, 1L);
    String ref = "ref";
    AlexandriaState state = AlexandriaState.CONFIRMED;
    boolean created = service.createOrUpdateResource(uuid, ref, provenance, state);
    assertThat(created).isTrue();
  }

  @Ignore
  @Test
  // TODO: fix test
  public void testDereferenceWithExistingAnnotation() {
    UUID id = UUID.randomUUID();
    AlexandriaAnnotationBody body = mock(AlexandriaAnnotationBody.class);
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    AnnotationVF annotationVF = mock(AnnotationVF.class);
    when(annotationVF.getUuid()).thenReturn(id.toString());
    when(annotationVF.getProvenanceWhen()).thenReturn(Instant.now().toString());
    when(mockStorage.readVF(AnnotationVF.class, id)).thenReturn(Optional.of(annotationVF));
    IdentifiablePointer<AlexandriaAnnotation> ap = new IdentifiablePointer<>(AlexandriaAnnotation.class, id.toString());

    Optional<? extends Accountable> optional = service.dereference(ap);
    assertThat(optional).isPresent();
    AlexandriaAnnotation annotation = (AlexandriaAnnotation) optional.get();
    assertThat(annotation.getId()).isEqualTo(id);
  }

  @Test
  public void testDereferenceWithNonExistingAnnotation() {
    UUID id = UUID.randomUUID();
    when(mockStorage.readVF(AnnotationVF.class, id)).thenReturn(Optional.empty());
    IdentifiablePointer<AlexandriaAnnotation> ap = new IdentifiablePointer<>(AlexandriaAnnotation.class, id.toString());

    Optional<? extends Accountable> optional = service.dereference(ap);
    assertThat(optional.isPresent()).isFalse();
  }

  @Ignore
  @Test
  // TODO: fix test
  public void testDereferenceWithExistingResource() {
    UUID id = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    // when(mockStorage.readResource(id)).thenReturn(Optional.of(new AlexandriaResource(id, provenance)));
    IdentifiablePointer<AlexandriaResource> ap = new IdentifiablePointer<>(AlexandriaResource.class, id.toString());

    Optional<? extends Accountable> optional = service.dereference(ap);
    assertThat(optional).isPresent();
    AlexandriaResource annotation = (AlexandriaResource) optional.get();
    assertThat(annotation.getId()).isEqualTo(id);
  }
}
