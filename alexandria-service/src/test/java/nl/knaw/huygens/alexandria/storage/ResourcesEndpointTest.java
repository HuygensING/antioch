package nl.knaw.huygens.alexandria.storage;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;

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
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourceEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcePrototype;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class ResourcesEndpointTest extends TinkergraphServiceEndpointTest {
  private static final String ROOTPATH = EndpointPaths.RESOURCES;
  ObjectMapper om = new ObjectMapper();

  @BeforeClass
  public static void registerEndpoint() {
    register(ResourcesEndpoint.class);
  }

  @Test
  public void testGettingANonExistingResourceGivesANotFoundError() {
    Response response = target(ROOTPATH).path("c28626d4-493a-4204-83d9-e9ae17e15654").request().get();
    Log.debug("response={}", response);
    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testPost() {
    TentativeAlexandriaProvenance provenance = mock(TentativeAlexandriaProvenance.class);
    Response response = target(ROOTPATH).request().post(jsonEntity("{'resource':{'ref':'REF'}}"));
    Log.debug("response={}", response);
    assertThat(response.getLocation().toString()).contains("/resources/");
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
  }

  @Test
  public void testPostResource() {
    // Object entity;
    // target(EndpointPaths.RESOURCES).request().post(Entity.json(entity));
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
