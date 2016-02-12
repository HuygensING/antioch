package nl.knaw.huygens.alexandria.service;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONIo;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.BaseLayerDefinition;
import nl.knaw.huygens.alexandria.model.BaseLayerDefinition.BaseElementDefinition;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.text.InMemoryTextService;

public class TinkerPopServiceTest {

  TinkerPopService service = new TinkerGraphService(new LocationBuilder(new MockConfiguration(), new EndpointPathResolver()), new InMemoryTextService());

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

  // @Test
  public void testDeleteTentativeAnnotationWithUniqueBodyRemovesAnnotationAndBody() {
    // TODO
    // TinkerPopService s = new TestStorage();
    AlexandriaAnnotation annotation = mock(AlexandriaAnnotation.class);
    service.deleteAnnotation(annotation);
  }

  // @Test
  public void testDeleteTentativeAnnotationWithSharedBodyRemovesAnnotationAndLeavesBody() {
    // TODO
    AlexandriaAnnotation annotation = mock(AlexandriaAnnotation.class);
    service.deleteAnnotation(annotation);
  }

  // @Test
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
    // given
    UUID resourceId = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    AlexandriaResource resource = new AlexandriaResource(resourceId, provenance);
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
    assertThat(newAnnotation.getAnnotatablePointer().getIdentifier()).isEqualTo(resourceId.toString());
    assertThat(newAnnotation.getProvenance().getWhen()).isEqualTo(provenance4.getWhen());
    assertThat(newAnnotation.getProvenance().getWho()).isEqualTo(provenance4.getWho());
    assertThat(newAnnotation.getProvenance().getWhy()).isEqualTo(provenance4.getWhy());
    assertThat(newAnnotation.getRevision()).isEqualTo(1);
  }

  private void logGraph(TinkerGraph graph) throws IOException {
    OutputStream os = new ByteArrayOutputStream();
    graph.io(new GraphSONIo.Builder()).writer().create().writeGraph(os, graph);
    Log.info("graph={}", os.toString());
  }

  // @Test
  // public void testTraversal() {
  // GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V();
  // traversal = traversal.and(hasLabel("Annotation"), hasLabel("Resource"))//
  // .values("uuid");
  // Map<String, Object> propertyMap = traversal.propertyMap().next();
  // }

  @Test
  public void testReturnExistingSubresourceIfSubPlusParentIdMatches() {
    // given
    UUID resourceId = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    AlexandriaResource resource = new AlexandriaResource(resourceId, provenance);
    service.createOrUpdateResource(resource);

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
    // given
    UUID resourceId = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    AlexandriaResource resource = new AlexandriaResource(resourceId, provenance);
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

  @Test
  public void testGetBaseLayerDefinitionForResourceReturnsTheFirstDefinitionUpTheResourceChain() {
    // given
    UUID resourceId = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    AlexandriaResource resource = new AlexandriaResource(resourceId, provenance);
    service.createOrUpdateResource(resource);
    List<BaseElementDefinition> baseElements = new ArrayList<>();
    baseElements.add(BaseElementDefinition.withName("text"));
    baseElements.add(BaseElementDefinition.withName("div"));
    service.setBaseLayerDefinition(resourceId, baseElements);

    UUID subUuid1 = UUID.randomUUID();
    String sub = "sub1";
    service.createSubResource(subUuid1, resourceId, sub, provenance);

    UUID subUuid2 = UUID.randomUUID();
    service.createSubResource(subUuid2, subUuid1, "sub2", provenance);

    Optional<BaseLayerDefinition> optDef = service.getBaseLayerDefinitionForResource(subUuid2);
    assertThat(optDef).isPresent();
    List<BaseElementDefinition> returnedBaseElementDefinitions = optDef.get().getBaseElementDefinitions();
    assertThat(returnedBaseElementDefinitions).hasSameSizeAs(baseElements);
    Log.info("base    ={}", baseElements);
    Log.info("returned={}", returnedBaseElementDefinitions);
    assertThat(returnedBaseElementDefinitions.get(0)).isEqualTo(baseElements.get(0));
    assertThat(returnedBaseElementDefinitions.get(1)).isEqualTo(baseElements.get(1));
  }

  @Test
  public void testGetBaseLayerDefinitionForResourceReturnsNullOptionalsWhenNoDefinitionPresentUpTheResourceChain() {
    // given
    UUID resourceId = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    AlexandriaResource resource = new AlexandriaResource(resourceId, provenance);
    service.createOrUpdateResource(resource);

    UUID subUuid1 = UUID.randomUUID();
    String sub = "sub1";
    service.createSubResource(subUuid1, resourceId, sub, provenance);

    UUID subUuid2 = UUID.randomUUID();
    service.createSubResource(subUuid2, subUuid1, "sub2", provenance);

    Optional<BaseLayerDefinition> optDef = service.getBaseLayerDefinitionForResource(subUuid2);
    assertThat(optDef.isPresent()).isFalse();
  }

}
