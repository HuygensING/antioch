package nl.knaw.huygens.alexandria.endpoint.resource;

import static nl.knaw.huygens.alexandria.endpoint.EndpointPaths.RESOURCES;

import java.util.UUID;
import java.util.function.Supplier;

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
import javax.ws.rs.WebApplicationException;
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
import nl.knaw.huygens.alexandria.exception.TentativeObjectException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Singleton
@Path(RESOURCES)
@Api(RESOURCES)
public class ResourcesEndpoint extends JSONEndpoint {

  private final AlexandriaService alexandriaService;
  private final ResourceEntityBuilder entityBuilder;
  private final ResourceCreationRequestBuilder requestBuilder;
  private final LocationBuilder locationBuilder;

  @Inject
  public ResourcesEndpoint(AlexandriaService service, //
                           ResourceCreationRequestBuilder requestBuilder, //
                           LocationBuilder locationBuilder, //
                           ResourceEntityBuilder entityBuilder) {
    this.locationBuilder = locationBuilder;
    this.alexandriaService = service;
    this.entityBuilder = entityBuilder;
    this.requestBuilder = requestBuilder;
    Log.trace("Resources created, alexandriaService=[{}]", service);
  }

  public static Supplier<NotFoundException> resourceNotFoundForId(Object id) {
    return () -> new NotFoundException("No resource found with id " + id);
  }

  public static WebApplicationException resourceIsTentativeException(UUID uuid) {
    return new TentativeObjectException("resource " + uuid + " is tentative, please confirm first");
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
    AlexandriaResource resource = request.execute(alexandriaService);

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
      alexandriaService.confirmResource(resource.getId());
      return noContent();
    }
    throw new BadRequestException("for now, you can only set the state to CONFIRMED");
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
  public Response setResourceAtSpecificID(@PathParam("uuid") final UUIDParam uuid, @NotNull @Valid @MatchesPathId
  ResourcePrototype protoType) {
    Log.trace("protoType=[{}]", protoType);

    protoType.setState(AlexandriaState.CONFIRMED);
    protoType.setId(uuid); // in case the prototype has no id, get it from the Path
    final ResourceCreationRequest request = requestBuilder.build(protoType);
    AlexandriaResource resource = request.execute(alexandriaService);

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

  @Path("{uuid}/provenance")
  public ResourceProvenanceEndpoint getProvenanceEndpoint(@PathParam("uuid") final UUIDParam uuidParam) {
    // If we let Jersey handle the lifecycle, this endpoint doesn't show up in the standard application.wadl
    // TODO: make the combination subresource as class/application.wadl work swagger seems to have the same problem
    return new ResourceProvenanceEndpoint(alexandriaService, uuidParam, locationBuilder);
  }

  private Response created(AlexandriaResource resource) {
    return created(locationBuilder.locationOf(resource));
  }

  private Response ok(AlexandriaResource resource) {
    return ok(entityBuilder.build(resource));
  }

  private AlexandriaResource readExistingResource(UUIDParam id) {
    return alexandriaService.readResource(id.getValue()).orElseThrow(resourceNotFoundForId(id));
  }
}
