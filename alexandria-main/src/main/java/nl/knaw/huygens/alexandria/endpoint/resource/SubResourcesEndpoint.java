package nl.knaw.huygens.alexandria.endpoint.resource;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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
import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class SubResourcesEndpoint extends JSONEndpoint {

  private final AlexandriaService service;
  private final LocationBuilder locationBuilder;
  private final ResourceCreationRequestBuilder requestBuilder;
  private final UUID parentId;

  @Inject
  public SubResourcesEndpoint(AlexandriaService service, //
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
    final List<AlexandriaResource> subresources = service.readSubResources(parentId);
    return ok(jsonWrap(subresources));
  }

  private Map<String, List<Map<String, SubResourceEntity>>> jsonWrap(final List<AlexandriaResource> subresources) {
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
    prototype.setState(AlexandriaState.TENTATIVE);
    SubResourceCreationRequest request = requestBuilder.build(parentId, prototype);
    AlexandriaResource resource = request.execute(service);
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
    prototype.setState(AlexandriaState.CONFIRMED);
    prototype.setId(subUuidParam);
    SubResourceCreationRequest request = requestBuilder.build(parentId, prototype);
    AlexandriaResource resource = request.execute(service);
    URI subresourceLocation = locationBuilder.locationOf(resource);
    if (request.newResourceWasCreated()) {
      return created(subresourceLocation);
    } else {
      return Response.noContent().header("Location", subresourceLocation).build();
    }
  }

  private SubResourceEntity toEntity(AlexandriaResource alexandriaResource) {
    return SubResourceEntity.of(alexandriaResource).withLocationBuilder(locationBuilder);
  }

}
