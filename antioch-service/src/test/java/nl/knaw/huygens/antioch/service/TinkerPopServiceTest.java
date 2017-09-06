package nl.knaw.huygens.antioch.service;

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
import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.config.MockConfiguration;
import nl.knaw.huygens.antioch.endpoint.EndpointPathResolver;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;
import nl.knaw.huygens.antioch.model.AntiochProvenance;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.model.TentativeAntiochProvenance;
import nl.knaw.huygens.antioch.test.AntiochTest;

public class TinkerPopServiceTest extends AntiochTest {

  final TinkerPopService service = new TinkerGraphService(new LocationBuilder(new MockConfiguration(), new EndpointPathResolver()));

  @Test
  public void testReadAfterCreateIsIdentity() {
    UUID id = UUID.randomUUID();
    String who = "who";
    Instant when = Instant.now();
    String why = "why";
    TentativeAntiochProvenance provenance = new TentativeAntiochProvenance(who, when, why);
    AntiochResource resource = new AntiochResource(id, provenance);
    resource.setCargo("reference");
    // logGraphAsJson();
    Log.info("resource={}", resource);
    service.createOrUpdateResource(resource);
    // logGraphAsJson();
    Log.info("after createOrUpdate");
    AntiochResource read = service.readResource(id).get();
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
    AntiochAnnotation annotation = mock(AntiochAnnotation.class);
    service.deleteAnnotation(annotation);
  }

  @Ignore
  @Test
  public void testDeleteTentativeAnnotationWithSharedBodyRemovesAnnotationAndLeavesBody() {
    // TODO
    AntiochAnnotation annotation = mock(AntiochAnnotation.class);
    service.deleteAnnotation(annotation);
  }

  @Ignore
  @Test
  public void testDeleteConfirmedAnnotationSetsStateToDeleted() {
    // TODO
    AntiochAnnotation annotation = mock(AntiochAnnotation.class);
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
    AntiochResource resource = aResource();
    service.createOrUpdateResource(resource);

    UUID annotationBodyId = UUID.randomUUID();
    TentativeAntiochProvenance provenance1 = new TentativeAntiochProvenance("who1", Instant.now(), "why1");
    AntiochAnnotationBody body1 = service.createAnnotationBody(annotationBodyId, "type", "value", provenance1);

    UUID annotationId = UUID.randomUUID();
    TentativeAntiochProvenance provenance2 = new TentativeAntiochProvenance("who2", Instant.now(), "why2");
    AntiochAnnotation annotation = new AntiochAnnotation(annotationId, body1, provenance2);
    service.annotateResourceWithAnnotation(resource, annotation);
    service.confirmAnnotation(annotationId);
    assertThat(annotation.getRevision()).isEqualTo(0);

    UUID annotationBodyId2 = UUID.randomUUID();
    TentativeAntiochProvenance provenance3 = new TentativeAntiochProvenance("who3", Instant.now(), "why3");
    AntiochAnnotationBody body2 = service.createAnnotationBody(annotationBodyId2, "type", "updated value", provenance3);
    TentativeAntiochProvenance provenance4 = new TentativeAntiochProvenance("who4", Instant.now(), "why4");
    AntiochAnnotation updatedAnnotation = new AntiochAnnotation(annotationId, body2, provenance4);

    // when
    AntiochAnnotation newAnnotation = service.deprecateAnnotation(annotationId, updatedAnnotation);

    // then expect
    assertThat(newAnnotation.getId()).isEqualTo(annotationId);
    assertThat(newAnnotation.getState()).isEqualTo(AntiochState.CONFIRMED);
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
    AntiochResource resource = aResource();
    service.createOrUpdateResource(resource);

    UUID resourceId = resource.getId();
    TentativeAntiochProvenance provenance = copyOf(resource.getProvenance());

    UUID subUuid = UUID.randomUUID();
    String sub = "sub";
    service.createSubResource(subUuid, resourceId, sub, provenance);

    Optional<AntiochResource> oResource = service.findSubresourceWithSubAndParentId(sub, resourceId);
    assertThat(oResource).isPresent();
    assertThat(oResource.get().getId()).isEqualTo(subUuid);

    // now, create a new resource
    UUID resourceId1 = UUID.randomUUID();
    AntiochResource resource1 = new AntiochResource(resourceId1, provenance);
    service.createOrUpdateResource(resource1);

    // I should be able to make a subresource on this new resource with the same value for sub
    Optional<AntiochResource> oResource1 = service.findSubresourceWithSubAndParentId(sub, resourceId1);
    assertThat(oResource1.isPresent()).isFalse();
  }

  @Test
  public void testDeletingAnAnnotationWithStateDeletedDoesNotFail() {
    AntiochResource resource = aResource();
    service.createOrUpdateResource(resource);

    UUID annotationBodyId = UUID.randomUUID();
    TentativeAntiochProvenance provenance1 = new TentativeAntiochProvenance("who1", Instant.now(), "why1");
    AntiochAnnotationBody body1 = service.createAnnotationBody(annotationBodyId, "type", "value", provenance1);

    UUID annotationId = UUID.randomUUID();
    TentativeAntiochProvenance provenance2 = new TentativeAntiochProvenance("who2", Instant.now(), "why2");
    AntiochAnnotation annotation = new AntiochAnnotation(annotationId, body1, provenance2);

    service.annotateResourceWithAnnotation(resource, annotation);
    annotation = service.readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AntiochState.TENTATIVE);

    service.confirmAnnotation(annotationId);
    annotation = service.readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AntiochState.CONFIRMED);

    service.deleteAnnotation(annotation);
    annotation = service.readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AntiochState.DELETED);

    service.deleteAnnotation(annotation);
    annotation = service.readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AntiochState.DELETED);
  }

  private TentativeAntiochProvenance copyOf(AntiochProvenance provenance) {
    return new TentativeAntiochProvenance(provenance.getWho(), provenance.getWhen(), provenance.getWhy());
  }

  private AntiochResource aResource() {
    UUID resourceId = UUID.randomUUID();
    TentativeAntiochProvenance provenance = new TentativeAntiochProvenance("who", Instant.now(), "why");
    return new AntiochResource(resourceId, provenance);
  }

}
