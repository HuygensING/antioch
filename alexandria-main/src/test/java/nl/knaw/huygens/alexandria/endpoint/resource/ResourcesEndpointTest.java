package nl.knaw.huygens.alexandria.endpoint.resource;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.MockedServiceEndpointTest;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

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
    TentativeAlexandriaProvenance provenance = mock(TentativeAlexandriaProvenance.class);
    UUID id = UUID.randomUUID();
    AlexandriaResource resource = new AlexandriaResource(id, provenance);
    when(SERVICE_MOCK.readResource(any(UUID.class))).thenReturn(Optional.of(resource));
    Response response = target(ROOTPATH).request().post(jsonEntity("{'resource':{'ref':'REF'}}"));
    Log.debug("response={}", response);
    assertThat(response.getLocation().toString()).contains("/resources/" + id);
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
  }

  // @Test
  // public void testPostResource() {
  // // Object entity;
  // // target(EndpointPaths.RESOURCES).request().post(Entity.json(entity));
  // }

  // @Test
  public void testTwoPutsWillNotProduceAnError() throws IOException {
    AlexandriaService service = mock(AlexandriaService.class);
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

  private ResourcePrototype deserialize(String json1) throws IOException {
    return om.readValue(json1.replace("'", "\""), ResourcePrototype.class);
  }
}
