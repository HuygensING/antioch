package nl.knaw.huygens.alexandria.storage;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationsEndpoint;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceAnnotationsEndpoint;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcePrototype;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class EndpointsTest extends TinkergraphServiceEndpointTest {
  private static final String ROOTPATH = EndpointPaths.RESOURCES;
  ObjectMapper om = new ObjectMapper();

  @BeforeClass
  public static void registerEndpoint() {
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
  public void testPostResourceSetsStateToTentativeAndPutSetsStateToConfirmed() {
    Response response = target(ROOTPATH).request().post(jsonEntity("{'resource':{'ref':'REF'}}"));
    Log.debug("response={}", response);
    assertThat(response.getLocation().toString()).contains("/resources/");
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    UUID id = extractId(response);
    Log.debug("uuid={}", id);
    AlexandriaResource resource = getStorage().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AlexandriaState.TENTATIVE);

    response = target(ROOTPATH).path(id.toString()).request().put(jsonEntity("{'resource':{'id':'" + id + "','ref':'REF'}}"));
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

    resource = getStorage().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AlexandriaState.CONFIRMED);
  }

  @Test
  public void testPostResourceAnnotationSetsStateToTentativeAndPutAnnotationSetsStateToConfirmed() {
    Response response = target(ROOTPATH).request().post(jsonEntity("{'resource':{'ref':'REF'}}"));
    Log.debug("response={}", response);
    assertThat(response.getLocation().toString()).contains("/resources/");
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    UUID id = extractId(response);
    Log.debug("resource uuid={}", id);
    AlexandriaResource resource = getStorage().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AlexandriaState.TENTATIVE);

    response = target(ROOTPATH).path(id.toString()).request().put(jsonEntity("{'resource':{'id':'" + id + "','ref':'REF'}}"));
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

    resource = getStorage().readResource(id).get();
    assertThat(resource.getState()).isEqualTo(AlexandriaState.CONFIRMED);

    response = target(ROOTPATH).path(id.toString()).path("annotations").request().post(jsonEntity("{'annotation':{'value':'bladiebla'}}"));
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    UUID annotationId = extractId(response);
    Log.debug("annotation uuid={}", annotationId);
    AlexandriaAnnotation annotation = getStorage().readAnnotation(annotationId).get();
    assertThat(annotation.getState()).isEqualTo(AlexandriaState.TENTATIVE);

    response = target("annotations").path(annotationId.toString()).request().put(jsonEntity("{'annotation':{'id':'" + id + "','value':'bladiebla'}}"));
    assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

    annotation = getStorage().readAnnotation(annotationId).get();
    // assertThat(annotation.getState()).isEqualTo(AlexandriaState.Default);
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

  // @Test
  public void testTwoPutsWillNotProduceAnError() throws JsonParseException, JsonMappingException, IOException {
    AlexandriaService service = mock(AlexandriaService.class);
    ResourceCreationRequestBuilder requestBuilder = new ResourceCreationRequestBuilder();
    LocationBuilder locationBuilder = new LocationBuilder(testConfiguration(), new EndpointPathResolver());
    ResourceEntityBuilder entityBuilder = new ResourceEntityBuilder(locationBuilder);
    ResourcesEndpoint re = new ResourcesEndpoint(service, requestBuilder, locationBuilder, entityBuilder);

    ResourcePrototype prototype1 = deserialize("{'resource':{'id':'c28626d4-493a-4204-83d9-e9ae17e15654','ref':'Referentie1'}}");
    Response response1 = re.setResourceAtSpecificID(prototype1);
    assertThat(response1.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    ResourcePrototype prototype2 = deserialize("{'resource':{'id':'d1753214-493a-4204-83d9-e9ae17e15654','ref':'Referentie2'}}");
    Response response2 = re.setResourceAtSpecificID(prototype2);
    assertThat(response2.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
  }

  private ResourcePrototype deserialize(String json1) throws JsonParseException, JsonMappingException, IOException {
    return om.readValue(json1.replace("'", "\""), ResourcePrototype.class);
  }
}
