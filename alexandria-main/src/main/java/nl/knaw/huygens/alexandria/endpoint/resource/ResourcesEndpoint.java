package nl.knaw.huygens.alexandria.endpoint.resource;

/*
 * #%L
 * alexandria-main
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

import static nl.knaw.huygens.alexandria.endpoint.EndpointPaths.RESOURCES;
import static nl.knaw.huygens.alexandria.endpoint.resource.ResourceValidatorFactory.resourceNotFoundForId;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.StatePrototype;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.exception.ConflictException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Singleton
@Path(RESOURCES)
@Api(RESOURCES)
public class ResourcesEndpoint extends JSONEndpoint {

  private final AlexandriaService service;
  private final ResourceEntityBuilder entityBuilder;
  private final ResourceCreationRequestBuilder requestBuilder;
  private final LocationBuilder locationBuilder;
  protected static final String BASELAYERDEFINITION = "baselayerdefinition";

  @Inject
  public ResourcesEndpoint(AlexandriaService service, //
      ResourceCreationRequestBuilder requestBuilder, //
      LocationBuilder locationBuilder, //
      ResourceEntityBuilder entityBuilder) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    this.entityBuilder = entityBuilder;
    this.requestBuilder = requestBuilder;
    Log.trace("Resources created, service=[{}]", service);
  }

  @GET
  @Path("{uuid}")
  @ApiOperation(value = "Get the resource with the given uuid", response = ResourceEntity.class)
  public Response getResourceByID(@PathParam("uuid") final UUIDParam uuidParam) {
    return ok(readExistingResource(uuidParam));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("create new Resource")
  public Response createResource(@NotNull @Valid @WithoutId ResourcePrototype protoType) {
    Log.trace("protoType=[{}]", protoType);

    protoType.setState(AlexandriaState.TENTATIVE);
    final ResourceCreationRequest request = requestBuilder.build(protoType);
    AlexandriaResource resource = request.execute(service);

    if (request.wasExecutedAsIs()) {
      return noContent();
    }

    return created(resource);
  }

  @PUT
  @Path("{uuid}/state")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "update the state of the resource (only state=CONFIRMED accepted for now)")
  public Response setResourceState(@PathParam("uuid") final UUIDParam uuidParam, @NotNull StatePrototype protoType) {
    Log.trace("protoType=[{}]", protoType);
    AlexandriaResource resource = readExistingResource(uuidParam);
    if (protoType.isConfirmed()) {
      if (!resource.isActive()) {
        throw new ConflictException(resource.getState() + " resources cannot be set to CONFIRMED");
      }
      service.confirmResource(resource.getId());
      return noContent();
    }
    throw new BadRequestException("for now, you can only set the state to CONFIRMED");
  }

  @PUT
  @Path("{uuid}/" + BASELAYERDEFINITION)
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Set the baselayer definition")
  public Response setBaseLayerDefinition(@PathParam("uuid") final UUIDParam uuidParam, @NotNull BaseLayerDefinitionPrototype protoType) {
    Log.trace("protoType=[{}]", protoType);
    AlexandriaResource resource = readExistingResource(uuidParam);
    if (resource.getDirectBaseLayerDefinition().isPresent()) {
      throw new ConflictException("This resource already has a baselayer definition");
    }
    service.setBaseLayerDefinition(uuidParam.getValue(), protoType.getBaseElementDefinitions());
    return created(URI.create(locationBuilder.locationOf(resource) + "/" + BASELAYERDEFINITION));
  }

  @GET
  @Path("{uuid}/" + BASELAYERDEFINITION)
  @ApiOperation(value = "Get the baselayer definition")
  public Response getBaseLayerDefinition(@PathParam("uuid") final UUIDParam uuidParam) {
    AlexandriaResource resource = readExistingResource(uuidParam);
    if (!resource.getDirectBaseLayerDefinition().isPresent()) {
      throw new NotFoundException("This resource has no baselayer definition"); // TODO: alternatively, throw redirected to ancestor baselayer definition (if any)
    }
    return ok(resource.getDirectBaseLayerDefinition().get());
  }

  @DELETE
  @Path("{uuid}")
  public Response deleteNotSupported(@PathParam("uuid") final UUIDParam paramId) {
    return methodNotImplemented();
  }

  @PUT
  @Path("{uuid}")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "update/create the resource with the given uuid")
  public Response setResourceAtSpecificID(@PathParam("uuid") final UUIDParam uuid, //
      @NotNull @Valid @MatchesPathId ResourcePrototype protoType) {
    Log.trace("protoType=[{}]", protoType);

    protoType.setState(AlexandriaState.CONFIRMED);
    protoType.setId(uuid); // in case the prototype has no id, get it from the Path
    final ResourceCreationRequest request = requestBuilder.build(protoType);
    AlexandriaResource resource = request.execute(service);

    if (request.newResourceWasCreated()) {
      return created(resource);
    }

    if (request.wasExecutedAsIs()) {
      return noContent();
    }

    return ok(resource);
  }

  // Sub-resource delegation

  @Path("{uuid}/subresources")
  public Class<SubResourcesEndpoint> getSubResourcesEndpoint() {
    return SubResourcesEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

  @Path("{uuid}/annotations")
  public Class<ResourceAnnotationsEndpoint> getAnnotationsEndpoint() {
    return ResourceAnnotationsEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

  @Path("{uuid}/text")
  public Class<ResourceTextEndpoint> getResourceTextEndpoint() {
    return ResourceTextEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

  @Path("{uuid}/provenance")
  public ResourceProvenanceEndpoint getProvenanceEndpoint(@PathParam("uuid") final UUIDParam uuidParam) {
    // If we let Jersey handle the lifecycle, this endpoint doesn't show up in the standard application.wadl
    // TODO: make the combination subresource as class/application.wadl work swagger seems to have the same problem
    return new ResourceProvenanceEndpoint(service, uuidParam, locationBuilder);
  }

  private Response created(AlexandriaResource resource) {
    return created(locationBuilder.locationOf(resource));
  }

  private Response ok(AlexandriaResource resource) {
    return ok(entityBuilder.build(resource));
  }

  private AlexandriaResource readExistingResource(UUIDParam id) {
    return service.readResource(id.getValue()).orElseThrow(resourceNotFoundForId(id));
  }
}
