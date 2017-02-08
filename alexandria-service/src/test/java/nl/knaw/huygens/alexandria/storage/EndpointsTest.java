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

import java.io.IOException;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationsEndpoint;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceAnnotationsEndpoint;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcePrototype;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class EndpointsTest extends TinkergraphServiceEndpointTest {
  private static final String ROOTPATH = EndpointPaths.RESOURCES;
  static ObjectMapper om = new ObjectMapper();

  @BeforeClass
  public static void registerEndpoint() {
    om.registerModule(new Jdk8Module());
    register(ResourcesEndpoint.class);
    register(AnnotationsEndpoint.class);
    register(ResourceAnnotationsEndpoint.class);
  }

  @Test
  public void testGettingANonExistingResourceGivesANotFoundError() {
    Response response = target(ROOTPATH).path("c28626d4-493a-4204-83d9-e9ae17e15654").request().get();
    Log.debug("response={}", response);
    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testUpdatingATentativeResourceLeadsToConflictException() {
    Response response = target(ROOTPATH).request().post(jsonEntity("{'resource':{'ref':'REF'}}"));
    Log.debug("response={}", response);
    assertThat(response.getLocation().toString()).contains("/resources/");
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    UUID id = extractId(response);
    Log.debug("uuid={}", id);
    AlexandriaResource resource = getService().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AlexandriaState.TENTATIVE);

    response = target(ROOTPATH).path(id.toString()).request().put(jsonEntity("{'resource':{'id':'" + id + "','ref':'REF'}}"));
    assertThat(response.getStatus()).isEqualTo(Status.CONFLICT.getStatusCode());

    resource = getService().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AlexandriaState.TENTATIVE);
  }

  @Test
  public void testPostResourceSetsStateToTentativeAndPutSetsStateToConfirmed() {
    Response response = target(ROOTPATH).request().post(jsonEntity("{'resource':{'ref':'REF'}}"));
    Log.debug("response={}", response);
    assertThat(response.getLocation().toString()).contains("/resources/");
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    UUID id = extractId(response);
    Log.debug("uuid={}", id);
    AlexandriaResource resource = getService().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AlexandriaState.TENTATIVE);

    getService().confirmResource(id);

    response = target(ROOTPATH).path(id.toString()).request().put(jsonEntity("{'resource':{'id':'" + id + "','ref':'REF'}}"));
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

    resource = getService().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AlexandriaState.CONFIRMED);
  }

  @Test
  public void testPostResourceAnnotationSetsStateToTentativeAndPutConfirmedToState() {
    Response response = target(ROOTPATH).request().post(jsonEntity("{'resource':{'ref':'REF'}}"));
    Log.debug("response={}", response);
    assertThat(response.getLocation().toString()).contains("/resources/");
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    UUID id = extractId(response);
    Log.debug("resource uuid={}", id);
    AlexandriaResource resource = getService().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AlexandriaState.TENTATIVE);

    response = target(ROOTPATH).path(id.toString()).path("state").request().put(jsonEntity("{'state':'CONFIRMED'}"));
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

    resource = getService().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AlexandriaState.CONFIRMED);

    response = target(ROOTPATH).path(id.toString()).path("annotations").request().post(jsonEntity("{'annotation':{'value':'bladiebla'}}"));
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    UUID annotationId = extractId(response);
    Log.debug("annotation uuid={}", annotationId);
    AlexandriaAnnotation annotation = getService().readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AlexandriaState.TENTATIVE);

    response = target("annotations").path(annotationId.toString()).path("state").request().put(jsonEntity("{'state':'CONFIRMED'}"));
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

    annotation = getService().readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AlexandriaState.CONFIRMED);
  }

  private UUID extractId(Response response) {
    String[] parts = response.getLocation().getPath().split("/");
    return UUID.fromString(parts[parts.length - 1]);
  }

  @Test
  public void testPutAndAnnotate() {
    UUID uuid = UUID.randomUUID();
    Entity<String> resourcePrototype = jsonEntity("{'resource':{'id':'" + uuid.toString() + "','ref':'Jan Klaassen'}}");
    Response response = target(ROOTPATH).path(uuid.toString()).request().put(resourcePrototype);
    Log.debug("response={}", response);
    assertThat(response.getLocation().toString()).contains("/resources/");
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    Entity<String> annotationPrototype = jsonEntity("{'annotation':{'type':'Tag','value':'Bookmark'}}");
    Response annotateResponse = target(ROOTPATH).path(uuid.toString()).path("annotations").request().post(annotationPrototype);
    Log.debug("response={}", annotateResponse);
    assertThat(annotateResponse.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
    Log.debug("response.location={}", annotateResponse.getLocation());
  }

  @Ignore
  @Test
  public void testTwoPutsWillNotProduceAnError() throws IOException {
    AlexandriaService service = mock(AlexandriaService.class);
    ResourceCreationRequestBuilder requestBuilder = new ResourceCreationRequestBuilder();
    LocationBuilder locationBuilder = new LocationBuilder(testConfiguration(), new EndpointPathResolver());
    ResourceEntityBuilder entityBuilder = new ResourceEntityBuilder(locationBuilder);
    ResourcesEndpoint re = new ResourcesEndpoint(service, requestBuilder, locationBuilder, entityBuilder);

    ResourcePrototype prototype1 = deserialize("{'resource':{'id':'c28626d4-493a-4204-83d9-e9ae17e15654','ref':'Referentie1'}}");
    UUIDParam uuidparam1 = new UUIDParam("c28626d4-493a-4204-83d9-e9ae17e15654");
    Response response1 = re.setResourceAtSpecificID(uuidparam1, prototype1);
    assertThat(response1.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    ResourcePrototype prototype2 = deserialize("{'resource':{'id':'d1753214-493a-4204-83d9-e9ae17e15654','ref':'Referentie2'}}");
    UUIDParam uuidparam2 = new UUIDParam("d1753214-493a-4204-83d9-e9ae17e15654");
    Response response2 = re.setResourceAtSpecificID(uuidparam2, prototype2);
    assertThat(response2.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
  }

  private ResourcePrototype deserialize(String json1) throws IOException {
    return om.readValue(fixQuotes(json1), ResourcePrototype.class);
  }
}
