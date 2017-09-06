package nl.knaw.huygens.antioch.storage;

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
import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.endpoint.EndpointPathResolver;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.endpoint.UUIDParam;
import nl.knaw.huygens.antioch.endpoint.annotation.AnnotationsEndpoint;
import nl.knaw.huygens.antioch.endpoint.resource.ResourceAnnotationsEndpoint;
import nl.knaw.huygens.antioch.endpoint.resource.ResourceCreationRequestBuilder;
import nl.knaw.huygens.antioch.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.antioch.endpoint.resource.ResourcePrototype;
import nl.knaw.huygens.antioch.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.service.AntiochService;

public class EndpointsTest extends TinkergraphServiceEndpointTest {
  private static final String ROOTPATH = EndpointPaths.RESOURCES;
  private static final ObjectMapper om = new ObjectMapper();

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
    AntiochResource resource = getService().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AntiochState.TENTATIVE);

    response = target(ROOTPATH).path(id.toString()).request().put(jsonEntity("{'resource':{'id':'" + id + "','ref':'REF'}}"));
    assertThat(response.getStatus()).isEqualTo(Status.CONFLICT.getStatusCode());

    resource = getService().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AntiochState.TENTATIVE);
  }

  @Test
  public void testPostResourceSetsStateToTentativeAndPutSetsStateToConfirmed() {
    Response response = target(ROOTPATH).request().post(jsonEntity("{'resource':{'ref':'REF'}}"));
    Log.debug("response={}", response);
    assertThat(response.getLocation().toString()).contains("/resources/");
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    UUID id = extractId(response);
    Log.debug("uuid={}", id);
    AntiochResource resource = getService().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AntiochState.TENTATIVE);

    getService().confirmResource(id);

    response = target(ROOTPATH).path(id.toString()).request().put(jsonEntity("{'resource':{'id':'" + id + "','ref':'REF'}}"));
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

    resource = getService().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AntiochState.CONFIRMED);
  }

  @Test
  public void testPostResourceAnnotationSetsStateToTentativeAndPutConfirmedToState() {
    Response response = target(ROOTPATH).request().post(jsonEntity("{'resource':{'ref':'REF'}}"));
    Log.debug("response={}", response);
    assertThat(response.getLocation().toString()).contains("/resources/");
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    UUID id = extractId(response);
    Log.debug("resource uuid={}", id);
    AntiochResource resource = getService().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AntiochState.TENTATIVE);

    response = target(ROOTPATH).path(id.toString()).path("state").request().put(jsonEntity("{'state':'CONFIRMED'}"));
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

    resource = getService().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AntiochState.CONFIRMED);

    response = target(ROOTPATH).path(id.toString()).path("annotations").request().post(jsonEntity("{'annotation':{'value':'bladiebla'}}"));
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    UUID annotationId = extractId(response);
    Log.debug("annotation uuid={}", annotationId);
    AntiochAnnotation annotation = getService().readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AntiochState.TENTATIVE);

    response = target("annotations").path(annotationId.toString()).path("state").request().put(jsonEntity("{'state':'CONFIRMED'}"));
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

    annotation = getService().readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AntiochState.CONFIRMED);
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
    AntiochService service = mock(AntiochService.class);
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
