package nl.knaw.huygens.alexandria.endpoint.resource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
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
  private static final URI HERE = URI.create("");

  private static final Logger LOG = LoggerFactory.getLogger(ResourcesEndpoint.class);

  private final ResourceService resourceService;
  private final ResourceEntityBuilder entityBuilder;
  private final ResourceCreationCommandBuilder commandBuilder;

  public ResourcesEndpoint(@Context ResourceService resourceService, @Context ResourceCreationCommandBuilder
      commandBuilder, @Context ResourceEntityBuilder entityBuilder) {
    LOG.trace("Resources created, resourceService=[{}]", resourceService);

    this.entityBuilder = entityBuilder;
    this.commandBuilder = commandBuilder;
    this.resourceService = resourceService;
  }

  @GET
  @Path("{uuid}")
  public Response getResourceByID(@PathParam("uuid") final UUIDParam uuid) {
    final AlexandriaResource resource = resourceService.readResource(uuid.getValue());
    return Response.ok(entityBuilder.build(resource)).build();
  }

  @POST
  public Response createResourceWithoutGivenID(ResourcePrototype protoType) {
    final ResourceCreationCommand command = commandBuilder.withoutId(protoType);
    final AlexandriaResource resource = command.execute(resourceService);

    if (command.requiredIntervention()) {
      final ResourceEntity entity = entityBuilder.build(resource);
      return Response.created(locationOf(resource)).entity(entity).build();
    }

    return Response.noContent().build();
  }

  @PUT
  @Path("{uuid}")
  public Response createResourceAtSpecificID(@PathParam("uuid") final UUIDParam paramId, ResourcePrototype protoType) {
    final ResourceCreationCommand command = commandBuilder.ofExistingId(protoType, paramId.getValue());
    final AlexandriaResource resource = command.execute(resourceService);

    if (command.requiredIntervention()) {
      final ResourceEntity entity = entityBuilder.build(resource);
      return Response.created(HERE).entity(entity).build();
    }

    return Response.noContent().build();
  }

  @Path("{uuid}/annotations")
  public ResourceAnnotations getAnnotationsForResource(@PathParam("uuid") final UUIDParam uuidParam) {
    return new ResourceAnnotations(resourceService, uuidParam.getValue());
  }

  @GET
  @Path("{uuid}/ref")
  public Response getResourceRef(@PathParam("uuid") final UUIDParam uuidParam) {
    AlexandriaResource resource = resourceService.readResource(uuidParam.getValue());
    return Response.ok(RefEntity.of(resource.getRef())).build();
  }

  private URI locationOf(AlexandriaResource resource) {
    return URI.create(resource.getId().toString());
  }

}
