package nl.knaw.huygens.alexandria.endpoint.resource;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.net.URI;

import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(EndpointPaths.RESOURCES)
public class ResourcesEndpoint extends JSONEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(JSONEndpoint.class);

  private static final URI HERE = URI.create("");

  private final ResourceService resourceService;
  private final ResourceEntityBuilder entityBuilder;
  private final ResourceCreationRequestBuilder requestBuilder;

  @Inject
  public ResourcesEndpoint(ResourceService resourceService, //
                           ResourceCreationRequestBuilder requestBuilder, //
                           ResourceEntityBuilder entityBuilder) {
    LOG.trace("Resources created, resourceService=[{}]", resourceService);

    this.entityBuilder = entityBuilder;
    this.requestBuilder = requestBuilder;
    this.resourceService = resourceService;
  }

  @GET
  @Path("{uuid}")
  public Response getResourceByID(@PathParam("uuid") final UUIDParam uuid) {
    final AlexandriaResource resource = resourceService.readResource(uuid.getValue());
    return Response.ok(entityBuilder.build(resource)).build();
  }

  @POST
  public Response createResourceWithoutGivenID(@NotNull @Valid @WithoutId ResourcePrototype protoType) {
    LOG.trace("protoType=[{}]", protoType);

    final ResourceCreationRequest request = requestBuilder.build(protoType);
    final AlexandriaResource resource = request.execute(resourceService);

    if (request.wasExecutedAsIs()) {
      return Response.noContent().build();
    }

    final ResourceEntity entity = entityBuilder.build(resource);
    return Response.created(locationOf(resource)).entity(entity).build();
  }

  @PUT
  @Path("{uuid}")
  public Response createResourceAtSpecificID(@NotNull @Valid @MatchesPathId ResourcePrototype protoType) {
    LOG.trace("protoType=[{}]", protoType);

    final ResourceCreationRequest request = requestBuilder.build(protoType);
    final AlexandriaResource resource = request.execute(resourceService);

    if (request.newResourceWasCreated()) {
      return Response.created(HERE).entity(entityBuilder.build(resource)).build();
    }

    if (request.wasExecutedAsIs()) {
      return Response.noContent().build();
    }

    return Response.ok(entityBuilder.build(resource)).build();
  }

  @DELETE
  @Path("{uuid}")
  public Response deleteNotSupported(@PathParam("uuid") final UUIDParam paramId) {
    return Response.status(501).build();
  }

  // TODO: replace with sub-resource analogous to {uuid}/annotations (see below)
  @GET
  @Path("{uuid}/ref")
  public Response getResourceRef(@PathParam("uuid") final UUIDParam uuidParam) {
    AlexandriaResource resource = resourceService.readResource(uuidParam.getValue());
    return Response.ok(new RefEntity(resource.getRef())).build();
  }

  // Sub-resource delegation

  @Path("{uuid}/annotations")
  public Class<ResourceAnnotations> getResourceAnnotations() {
    return ResourceAnnotations.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

  // TODO: replace by injected LocationBuilder (to be written) ?

  private URI locationOf(AlexandriaResource resource) {
    return URI.create(resource.getId().toString());
  }

}
