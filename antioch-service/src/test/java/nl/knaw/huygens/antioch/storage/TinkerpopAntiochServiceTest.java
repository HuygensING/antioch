package nl.knaw.huygens.antioch.storage;

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
 * antioch-service
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
import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.config.MockConfiguration;
import nl.knaw.huygens.antioch.endpoint.EndpointPathResolver;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.model.Accountable;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.model.IdentifiablePointer;
import nl.knaw.huygens.antioch.model.TentativeAntiochProvenance;
import nl.knaw.huygens.antioch.service.TinkerPopService;
import nl.knaw.huygens.antioch.storage.frames.AnnotationVF;

public class TinkerpopAntiochServiceTest {
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
    TentativeAntiochProvenance provenance = new TentativeAntiochProvenance("who", Instant.ofEpochSecond(1000000), "why");
    UUID uuid = new UUID(1L, 1L);
    String ref = "ref";
    AntiochState state = AntiochState.CONFIRMED;
    boolean created = service.createOrUpdateResource(uuid, ref, provenance, state);
    assertThat(created).isTrue();
  }

  @Ignore
  @Test
  // TODO: fix test
  public void testDereferenceWithExistingAnnotation() {
    UUID id = UUID.randomUUID();
    AntiochAnnotationBody body = mock(AntiochAnnotationBody.class);
    TentativeAntiochProvenance provenance = new TentativeAntiochProvenance("who", Instant.now(), "why");
    AnnotationVF annotationVF = mock(AnnotationVF.class);
    when(annotationVF.getUuid()).thenReturn(id.toString());
    when(annotationVF.getProvenanceWhen()).thenReturn(Instant.now().toString());
    when(mockStorage.readVF(AnnotationVF.class, id)).thenReturn(Optional.of(annotationVF));
    IdentifiablePointer<AntiochAnnotation> ap = new IdentifiablePointer<>(AntiochAnnotation.class, id.toString());

    Optional<? extends Accountable> optional = service.dereference(ap);
    assertThat(optional).isPresent();
    AntiochAnnotation annotation = (AntiochAnnotation) optional.get();
    assertThat(annotation.getId()).isEqualTo(id);
  }

  @Test
  public void testDereferenceWithNonExistingAnnotation() {
    UUID id = UUID.randomUUID();
    when(mockStorage.readVF(AnnotationVF.class, id)).thenReturn(Optional.empty());
    IdentifiablePointer<AntiochAnnotation> ap = new IdentifiablePointer<>(AntiochAnnotation.class, id.toString());

    Optional<? extends Accountable> optional = service.dereference(ap);
    assertThat(optional.isPresent()).isFalse();
  }

  @Ignore
  @Test
  // TODO: fix test
  public void testDereferenceWithExistingResource() {
    UUID id = UUID.randomUUID();
    TentativeAntiochProvenance provenance = new TentativeAntiochProvenance("who", Instant.now(), "why");
    // when(mockStorage.readResource(id)).thenReturn(Optional.of(new AntiochResource(id, provenance)));
    IdentifiablePointer<AntiochResource> ap = new IdentifiablePointer<>(AntiochResource.class, id.toString());

    Optional<? extends Accountable> optional = service.dereference(ap);
    assertThat(optional).isPresent();
    AntiochResource annotation = (AntiochResource) optional.get();
    assertThat(annotation.getId()).isEqualTo(id);
  }
}
