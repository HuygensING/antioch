package nl.knaw.huygens.alexandria.storage;

/*
 * #%L
 * alexandria-service
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.IdentifiablePointer;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.TinkerPopService;
import nl.knaw.huygens.alexandria.storage.frames.AnnotationVF;

public class TinkerpopAlexandriaServiceTest {
  private Storage mockStorage = mock(Storage.class);
  private final TinkerPopService service = new TinkerPopService(mockStorage, new LocationBuilder(new MockConfiguration(), new EndpointPathResolver()));

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
    AnnotationVF annotationVF = mock(AnnotationVF.class);
    when(annotationVF.getUuid()).thenReturn(id.toString());
    when(mockStorage.readVF(AnnotationVF.class, id)).thenReturn(Optional.of(annotationVF));
    IdentifiablePointer<AlexandriaAnnotation> ap = new IdentifiablePointer<>(AlexandriaAnnotation.class, id.toString());

    Optional<? extends Accountable> optional = service.dereference(ap);
    assertThat(optional.isPresent());
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

  // @Test
  // TODO: fix test
  public void testDereferenceWithExistingResource() {
    UUID id = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    // when(mockStorage.readResource(id)).thenReturn(Optional.of(new AlexandriaResource(id, provenance)));
    IdentifiablePointer<AlexandriaResource> ap = new IdentifiablePointer<>(AlexandriaResource.class, id.toString());

    Optional<? extends Accountable> optional = service.dereference(ap);
    assertThat(optional.isPresent());
    AlexandriaResource annotation = (AlexandriaResource) optional.get();
    assertThat(annotation.getId()).isEqualTo(id);
  }
}
