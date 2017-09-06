package nl.knaw.huygens.antioch.endpoint.resource;

/*
 * #%L
 * antioch-main
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.config.MockConfiguration;
import nl.knaw.huygens.antioch.endpoint.EndpointPathResolver;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.endpoint.MockedServiceEndpointTest;
import nl.knaw.huygens.antioch.endpoint.UUIDParam;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.model.TentativeAntiochProvenance;
import nl.knaw.huygens.antioch.service.AntiochService;

public class ResourcesEndpointTest extends MockedServiceEndpointTest {
  private static final String ROOTPATH = EndpointPaths.RESOURCES;

  private final ObjectMapper om = new ObjectMapper();

  @BeforeClass
  public static void registerEndpoint() {
    register(ResourcesEndpoint.class);
  }

  @Test
  public void getResourcesRefNoLongerExists() {
    when(SERVICE_MOCK.readResource(any(UUID.class))).thenReturn(Optional.empty());
    Response response = target(ROOTPATH).path(UUID.randomUUID().toString()).path("/ref").request().get();
    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testGettingANonExistingResourceGivesANotFoundError() {
    when(SERVICE_MOCK.readResource(any(UUID.class))).thenReturn(Optional.empty());
    Response response = target(ROOTPATH).path("c28626d4-493a-4204-83d9-e9ae17e15654").request().get();
    Log.debug("response={}", response);
    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testPost() {
    TentativeAntiochProvenance provenance = mock(TentativeAntiochProvenance.class);
    UUID id = UUID.randomUUID();
    AntiochResource resource = new AntiochResource(id, provenance);
    when(SERVICE_MOCK.readResource(any(UUID.class))).thenReturn(Optional.of(resource));
    Response response = target(ROOTPATH).request().post(jsonEntity("{'resource':{'ref':'REF'}}"));
    Log.debug("response={}", response);
    assertThat(response.getLocation().toString()).contains("/resources/" + id);
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
  }

  // @Ignore @Test
  // public void testPostResource() {
  // // Object entity;
  // // target(EndpointPaths.RESOURCES).request().post(Entity.json(entity));
  // }

  @Ignore
  @Test
  public void testTwoPutsWillNotProduceAnError() throws IOException {
    AntiochService service = mock(AntiochService.class);
    ResourceCreationRequestBuilder requestBuilder = new ResourceCreationRequestBuilder();
    LocationBuilder locationBuilder = new LocationBuilder(new MockConfiguration(), new EndpointPathResolver());
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

  // private methods

  private ResourcePrototype deserialize(String json1) throws IOException {
    return om.readValue(fixQuotes(json1), ResourcePrototype.class);
  }
}
