package nl.knaw.huygens.alexandria.endpoint.resource;

import static nl.knaw.huygens.alexandria.endpoint.EndpointPaths.RESOURCES;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
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

  @GET
  @Path("{uuid}")
  @ApiOperation(value = "Get the resource with the given uuid", response = ResourceEntity.class)
  public Response getResourceByID(@PathParam("uuid") final UUIDParam uuid) {
    AlexandriaResource resource = alexandriaService.readResource(uuid.getValue())//
        .orElseThrow(resourceNotFoundForId(uuid));
    return Response.ok(entityBuilder.build(resource)).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("create new Resource")
  public Response createResource(@NotNull @Valid @WithoutId ResourcePrototype protoType) {
    Log.trace("protoType=[{}]", protoType);

    protoType.setState(AlexandriaState.Temporary);
    final ResourceCreationRequest request = requestBuilder.build(protoType);
    AlexandriaResource resource = request.execute(alexandriaService);

    if (request.wasExecutedAsIs()) {
      return Response.noContent().build();
    }

    // final ResourceEntity entity = entityBuilder.build(resource);
    return Response.created(locationBuilder.locationOf(resource)).build();
  }

  @PUT
  @Path("{uuid}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response setResourceAtSpecificID(@NotNull @Valid @MatchesPathId ResourcePrototype protoType) {
    Log.trace("protoType=[{}]", protoType);

    protoType.setState(AlexandriaState.Default);
    final ResourceCreationRequest request = requestBuilder.build(protoType);
    AlexandriaResource resource = request.execute(alexandriaService);

    if (request.newResourceWasCreated()) {
      return Response.created(locationBuilder.locationOf(resource)).build();
    }

    if (request.wasExecutedAsIs()) {
      return Response.noContent().build();
    }

    return Response.ok().build();
  }

  @DELETE
  @Path("{uuid}")
  public Response deleteNotSupported(@PathParam("uuid") final UUIDParam paramId) {
    return methodNotImplemented();
  }

  // TODO: replace with sub-resource analogous to {uuid}/annotations (see below)
  @GET
  @Path("{uuid}/ref")
  @ApiOperation(value = "Get just the ref of the resource with the given uuid", response = RefEntity.class)
  public Response getResourceRef(@PathParam("uuid") final UUIDParam uuidParam) {
    AlexandriaResource resource = alexandriaService.readResource(uuidParam.getValue())//
        .orElseThrow(resourceNotFoundForId(uuidParam));
    return Response.ok(new RefEntity(resource.getCargo())).build();
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

  // @Path("{uuid}/provenance")
  // public Class<ResourceProvenanceEndpoint> getProvenanceEndpoint() {
  // return ResourceProvenanceEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  // }

  @Path("{uuid}/provenance")
  public ResourceProvenanceEndpoint getProvenanceEndpoint(@PathParam("uuid") final UUIDParam uuidParam) {
    // If we let Jersey handle the lifecycle, this endpoint doesn't show up in the standard application.wadl
    // TODO: make the combination subresource as class/application.wadl work
    // swagger seems to have the same problem
    return new ResourceProvenanceEndpoint(alexandriaService, uuidParam, locationBuilder);
  }

  public static Supplier<NotFoundException> resourceNotFoundForId(Object id) {
    return () -> new NotFoundException("No resource found with id " + id);
  };

}
