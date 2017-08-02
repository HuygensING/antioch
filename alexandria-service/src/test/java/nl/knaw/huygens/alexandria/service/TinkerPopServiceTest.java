package nl.knaw.huygens.alexandria.service;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONIo;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Ignore;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class TinkerPopServiceTest extends AlexandriaTest {

  TinkerPopService service = new TinkerGraphService(new LocationBuilder(new MockConfiguration(), new EndpointPathResolver()));

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
    service.createOrUpdateResource(resource);
    // logGraphAsJson();
    Log.info("after createOrUpdate");
    AlexandriaResource read = service.readResource(id).get();
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

  TinkerGraph graph = TinkerGraph.open();

  // class TestStorage extends TinkerPopService {
  // public TestStorage() {
  // super(graph);
  // }
  // }

  @Ignore
  @Test
  public void testDeleteTentativeAnnotationWithUniqueBodyRemovesAnnotationAndBody() {
    // TODO
    // TinkerPopService s = new TestStorage();
    AlexandriaAnnotation annotation = mock(AlexandriaAnnotation.class);
    service.deleteAnnotation(annotation);
  }

  @Ignore
  @Test
  public void testDeleteTentativeAnnotationWithSharedBodyRemovesAnnotationAndLeavesBody() {
    // TODO
    AlexandriaAnnotation annotation = mock(AlexandriaAnnotation.class);
    service.deleteAnnotation(annotation);
  }

  @Ignore
  @Test
  public void testDeleteConfirmedAnnotationSetsStateToDeleted() {
    // TODO
    AlexandriaAnnotation annotation = mock(AlexandriaAnnotation.class);
    service.deleteAnnotation(annotation);
  }

  @Test
  public void testGraphDrop() throws IOException {
    TinkerGraph graph = TinkerGraph.open();
    Vertex a1 = graph.addVertex("A");
    a1.property("key", "value");
    Vertex a2 = graph.addVertex("A");
    Vertex b1 = graph.addVertex("B");
    b1.addEdge("knows", a1, "property1", "value1");
    logGraph(graph);

    graph.traversal().V().hasLabel("A").forEachRemaining(Element::remove);
    logGraph(graph);
  }

  @Test
  public void testUuid() {
    UUID u = UUID.fromString("11111111-1111-1111-1111-111111111111");
    assertThat(u).isNotNull();
  }

  @Test
  public void testDeprecateAnnotation() {
    AlexandriaResource resource = aResource();
    service.createOrUpdateResource(resource);

    UUID annotationBodyId = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance1 = new TentativeAlexandriaProvenance("who1", Instant.now(), "why1");
    AlexandriaAnnotationBody body1 = service.createAnnotationBody(annotationBodyId, "type", "value", provenance1);

    UUID annotationId = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance2 = new TentativeAlexandriaProvenance("who2", Instant.now(), "why2");
    AlexandriaAnnotation annotation = new AlexandriaAnnotation(annotationId, body1, provenance2);
    service.annotateResourceWithAnnotation(resource, annotation);
    service.confirmAnnotation(annotationId);
    assertThat(annotation.getRevision()).isEqualTo(0);

    UUID annotationBodyId2 = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance3 = new TentativeAlexandriaProvenance("who3", Instant.now(), "why3");
    AlexandriaAnnotationBody body2 = service.createAnnotationBody(annotationBodyId2, "type", "updated value", provenance3);
    TentativeAlexandriaProvenance provenance4 = new TentativeAlexandriaProvenance("who4", Instant.now(), "why4");
    AlexandriaAnnotation updatedAnnotation = new AlexandriaAnnotation(annotationId, body2, provenance4);

    // when
    AlexandriaAnnotation newAnnotation = service.deprecateAnnotation(annotationId, updatedAnnotation);

    // then expect
    assertThat(newAnnotation.getId()).isEqualTo(annotationId);
    assertThat(newAnnotation.getState()).isEqualTo(AlexandriaState.CONFIRMED);
    assertThat(newAnnotation.getBody().getType()).isEqualTo("type");
    assertThat(newAnnotation.getBody().getValue()).isEqualTo("updated value");
    assertThat(newAnnotation.getAnnotatablePointer().getIdentifier()).isEqualTo(resource.getId().toString());
    assertThat(newAnnotation.getProvenance().getWhen()).isEqualTo(provenance4.getWhen());
    assertThat(newAnnotation.getProvenance().getWho()).isEqualTo(provenance4.getWho());
    assertThat(newAnnotation.getProvenance().getWhy()).isEqualTo(provenance4.getWhy());
    assertThat(newAnnotation.getRevision()).isEqualTo(1);
  }

  private void logGraph(TinkerGraph graph) throws IOException {
    OutputStream os = new ByteArrayOutputStream();
    graph.io(GraphSONIo.build()).writer().create().writeGraph(os, graph);
    Log.info("graph={}", os.toString());
  }

  // @Ignore
  // @Test
  // public void testTraversal() {
  // GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V();
  // traversal = traversal.and(hasLabel("Annotation"), hasLabel("Resource"))//
  // .values("uuid");
  // Map<String, Object> propertyMap = traversal.propertyMap().next();
  // }

  @Test
  public void testReturnExistingSubresourceIfSubPlusParentIdMatches() {
    AlexandriaResource resource = aResource();
    service.createOrUpdateResource(resource);

    UUID resourceId = resource.getId();
    TentativeAlexandriaProvenance provenance = copyOf(resource.getProvenance());

    UUID subUuid = UUID.randomUUID();
    String sub = "sub";
    service.createSubResource(subUuid, resourceId, sub, provenance);

    Optional<AlexandriaResource> oResource = service.findSubresourceWithSubAndParentId(sub, resourceId);
    assertThat(oResource).isPresent();
    assertThat(oResource.get().getId()).isEqualTo(subUuid);

    // now, create a new resource
    UUID resourceId1 = UUID.randomUUID();
    AlexandriaResource resource1 = new AlexandriaResource(resourceId1, provenance);
    service.createOrUpdateResource(resource1);

    // I should be able to make a subresource on this new resource with the same value for sub
    Optional<AlexandriaResource> oResource1 = service.findSubresourceWithSubAndParentId(sub, resourceId1);
    assertThat(oResource1.isPresent()).isFalse();
  }

  @Test
  public void testDeletingAnAnnotationWithStateDeletedDoesNotFail() {
    AlexandriaResource resource = aResource();
    service.createOrUpdateResource(resource);

    UUID annotationBodyId = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance1 = new TentativeAlexandriaProvenance("who1", Instant.now(), "why1");
    AlexandriaAnnotationBody body1 = service.createAnnotationBody(annotationBodyId, "type", "value", provenance1);

    UUID annotationId = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance2 = new TentativeAlexandriaProvenance("who2", Instant.now(), "why2");
    AlexandriaAnnotation annotation = new AlexandriaAnnotation(annotationId, body1, provenance2);

    service.annotateResourceWithAnnotation(resource, annotation);
    annotation = service.readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AlexandriaState.TENTATIVE);

    service.confirmAnnotation(annotationId);
    annotation = service.readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AlexandriaState.CONFIRMED);

    service.deleteAnnotation(annotation);
    annotation = service.readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AlexandriaState.DELETED);

    service.deleteAnnotation(annotation);
    annotation = service.readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AlexandriaState.DELETED);
  }

  private TentativeAlexandriaProvenance copyOf(AlexandriaProvenance provenance) {
    return new TentativeAlexandriaProvenance(provenance.getWho(), provenance.getWhen(), provenance.getWhy());
  }

  private AlexandriaResource aResource() {
    UUID resourceId = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    return new AlexandriaResource(resourceId, provenance);
  }

}
