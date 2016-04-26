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
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONIo;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import jersey.repackaged.com.google.common.collect.Lists;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.ElementDefinition;
import nl.knaw.huygens.alexandria.api.model.TextView;
import nl.knaw.huygens.alexandria.api.model.TextViewPrototype;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.command.CommandResponse;
import nl.knaw.huygens.alexandria.endpoint.command.WrapContentInElementCommand;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.test.AlexandriaTest;
import nl.knaw.huygens.alexandria.textgraph.ParseResult;
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil;

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
    graph.io(new GraphSONIo.Builder()).writer().create().writeGraph(os, graph);
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

  @Test
  public void testGetTextViewsForResourceReturnsTheFirstDefinitionUpTheResourceChain() {
    AlexandriaResource resource = aResource();
    service.createOrUpdateResource(resource);
    ElementDefinition bedText = ElementDefinition.withName("text");
    ElementDefinition bedDiv = ElementDefinition.withName("div");
    List<ElementDefinition> list = Lists.newArrayList(bedText, bedDiv);
    TextViewPrototype prototype = new TextViewPrototype().setIncludedElements(list);
    UUID resourceId = resource.getId();
    service.setTextView(resourceId, "baselayer", prototype);

    UUID subUuid1 = UUID.randomUUID();
    String sub = "sub1";
    TentativeAlexandriaProvenance provenance = copyOf(resource.getProvenance());
    service.createSubResource(subUuid1, resourceId, sub, provenance);

    UUID subUuid2 = UUID.randomUUID();
    service.createSubResource(subUuid2, subUuid1, "sub2", provenance);

    List<TextView> views = service.getTextViewsForResource(subUuid2);
    assertThat(views).isNotEmpty();
    List<ElementDefinition> returnedElementDefinitions = views.get(0).getIncludedElementDefinitions();
    assertThat(returnedElementDefinitions).containsExactly(bedText, bedDiv);
  }

  private TentativeAlexandriaProvenance copyOf(AlexandriaProvenance provenance) {
    return new TentativeAlexandriaProvenance(provenance.getWho(), provenance.getWhen(), provenance.getWhy());
  }

  @Test
  public void testGetTextViewsForResourceReturnsNullOptionalsWhenNoDefinitionPresentUpTheResourceChain() {
    AlexandriaResource resource = aResource();
    service.createOrUpdateResource(resource);

    UUID resourceId = resource.getId();
    TentativeAlexandriaProvenance provenance = copyOf(resource.getProvenance());

    UUID subUuid1 = UUID.randomUUID();
    String sub = "sub1";
    service.createSubResource(subUuid1, resourceId, sub, provenance);

    UUID subUuid2 = UUID.randomUUID();
    service.createSubResource(subUuid2, subUuid1, "sub2", provenance);

    List<TextView> textViews = service.getTextViewsForResource(subUuid2);
    assertThat(textViews).isEmpty();
  }

  @Test
  public void testXmlInEqualsXmlOut() throws WebApplicationException, IOException {
    // given
    String xml = singleQuotesToDouble("<xml><p xml:id='p-1' a='A' z='Z' b='B'>Bla</p></xml>");
    UUID resourceId = aResourceUUIDWithXml(xml);

    // when
    String out = getResourceXml(resourceId);

    // then
    assertThat(out).isEqualTo(xml);
  }

  @Test
  public void testWrapContentInElementWorks() throws WebApplicationException, IOException {
    // given
    String xml = singleQuotesToDouble("<text>"//
        + "<div xml:id='div-1'><p xml:id='p-1'>Paragraph the First.</p></div>"//
        + "<div xml:id='div-2'><p xml:id='p-2'>Paragraph the Second.</p></div>"//
        + "</text>");
    String expected = singleQuotesToDouble("<text>"//
        + "<div xml:id='div-1'><hi rend='blue'><p xml:id='p-1'>Paragraph the First.</p></hi></div>"//
        + "<div xml:id='div-2'><p xml:id='p-2'><hi rend='blue'>Paragraph the Second.</hi></p></div>"//
        + "</text>");
    UUID resourceId = aResourceUUIDWithXml(xml);

    WrapContentInElementCommand command = new WrapContentInElementCommand(service);
    ImmutableMap<String, Serializable> elementMap = ImmutableMap.of(//
        "name", "hi", //
        "attributes", ImmutableMap.of("rend", "blue")//
    );
    Map<String, Object> parameterMap = ImmutableMap.<String, Object> builder()//
        .put("resourceIds", Lists.newArrayList(resourceId.toString()))//
        .put("xmlIds", Lists.newArrayList("div-1", "p-2"))//
        .put("element", elementMap)//
        .build();

    // when
    CommandResponse response = command.runWith(parameterMap);

    // then
    assertThat(response.getErrorLines()).isEmpty();
    assertThat(response.paremetersAreValid()).isTrue();

    String out = getResourceXml(resourceId);
    assertThat(out).isEqualTo(expected);
  }

  private AlexandriaResource aResource() {
    UUID resourceId = UUID.randomUUID();
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
    AlexandriaResource resource = new AlexandriaResource(resourceId, provenance);
    return resource;
  }

  private UUID aResourceUUIDWithXml(String xml) {
    AlexandriaResource resource = aResource();
    service.createOrUpdateResource(resource);

    UUID resourceId = resource.getId();
    ParseResult result = TextGraphUtil.parse(xml);
    service.storeTextGraph(resourceId, result);
    return resourceId;
  }

  private String getResourceXml(UUID resourceId) throws IOException {
    StreamingOutput streamXML = TextGraphUtil.streamXML(service, resourceId);
    OutputStream output = new ByteArrayOutputStream();
    streamXML.write(output);
    output.flush();
    return output.toString();
  }

}
