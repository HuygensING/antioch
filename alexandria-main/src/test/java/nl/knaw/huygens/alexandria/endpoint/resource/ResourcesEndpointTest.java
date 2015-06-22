package nl.knaw.huygens.alexandria.endpoint.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResourcesEndpointTest {
  ObjectMapper om = new ObjectMapper();

  // @Test
  public void testTwoPutsWillNotProduceAnError() throws JsonParseException, JsonMappingException, IOException {
    AlexandriaService service = Mockito.mock(AlexandriaService.class);
    ResourceCreationRequestBuilder requestBuilder = new ResourceCreationRequestBuilder();
    LocationBuilder locationBuilder = new LocationBuilder(new MockConfiguration(), new EndpointPathResolver());
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
