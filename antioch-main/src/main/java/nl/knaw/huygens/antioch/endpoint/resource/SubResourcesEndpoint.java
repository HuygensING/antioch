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

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.ImmutableMap;

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.antioch.api.JsonTypeNames;
import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.endpoint.JSONEndpoint;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.endpoint.UUIDParam;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.service.AntiochService;

public class SubResourcesEndpoint extends JSONEndpoint {

  private final AntiochService service;
  private final LocationBuilder locationBuilder;
  private final ResourceCreationRequestBuilder requestBuilder;
  private final UUID parentId;

  @Inject
  public SubResourcesEndpoint(AntiochService service, //
      LocationBuilder locationBuilder, //
      ResourceCreationRequestBuilder requestBuilder, //
      ResourceValidatorFactory validatorFactory, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    this.requestBuilder = requestBuilder;
    this.parentId = validatorFactory.validateExistingResource(uuidParam).notTentative().get().getId();
  }

  @GET
  @ApiOperation(value = "get subresources", response = ResourceEntity.class)
  public Response get() {
    final List<AntiochResource> subresources = service.readSubResources(parentId);
    return ok(jsonWrap(subresources));
  }

  private Map<String, List<Map<String, SubResourceEntity>>> jsonWrap(final List<AntiochResource> subresources) {
    return ImmutableMap.of(//
        JsonTypeNames.SUBRESOURCELIST,
        subresources.stream()//
            .map(this::toEntity)//
            .map(this::toSubResourceMap)//
            .collect(toList())//
    );
  }

  private Map<String, SubResourceEntity> toSubResourceMap(SubResourceEntity e) {
    return ImmutableMap.of(JsonTypeNames.SUBRESOURCE, e);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("add subresource")
  public Response addSubResource(@NotNull @Valid SubResourcePrototype prototype) {
    prototype.setState(AntiochState.TENTATIVE);
    SubResourceCreationRequest request = requestBuilder.build(parentId, prototype);
    AntiochResource resource = request.execute(service);
    URI subresourceLocation = locationBuilder.locationOf(resource);
    if (request.newResourceWasCreated()) {
      return created(subresourceLocation);
    } else {
      return Response.noContent().header("Location", subresourceLocation).build();
    }
  }

  @PUT
  @Path("{subuuid}")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("set subresource")
  public Response setSubResource(@PathParam("subuuid") final UUIDParam subUuidParam, @NotNull @Valid SubResourcePrototype prototype) {
    prototype.setState(AntiochState.CONFIRMED);
    prototype.setId(subUuidParam);
    SubResourceCreationRequest request = requestBuilder.build(parentId, prototype);
    AntiochResource resource = request.execute(service);
    URI subresourceLocation = locationBuilder.locationOf(resource);
    if (request.newResourceWasCreated()) {
      return created(subresourceLocation);
    } else {
      return Response.noContent().header("Location", subresourceLocation).build();
    }
  }

  private SubResourceEntity toEntity(AntiochResource antiochResource) {
    return SubResourceEntity.of(antiochResource).withLocationBuilder(locationBuilder);
  }

}
