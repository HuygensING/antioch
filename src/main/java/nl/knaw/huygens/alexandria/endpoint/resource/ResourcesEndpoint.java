package nl.knaw.huygens.alexandria.endpoint.resource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
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
  private final AlexandriaConfiguration config;
  private final ResourceEntityBuilder entityBuilder;

  public ResourcesEndpoint(@Context AlexandriaConfiguration config, //
                           @Context ResourceService resourceService,
                           @Context ResourceEntityBuilder entityBuilder) {
    LOG.trace("Resources created, resourceService=[{}], config=[{}]", resourceService, config);

    this.config = config;
    this.entityBuilder = entityBuilder;
    this.resourceService = resourceService;
  }

  @GET
  @Path("{uuid}")
  public Response getResourceByID(@PathParam("uuid") final UUIDParam uuid) {
    final AlexandriaResource resource = resourceService.readResource(uuid.getValue());
    return Response.ok(entityBuilder.build(resource)).build();
  }

  @POST
  public Response createResourceWithoutGivenID(AlexandriaResource protoType) {
    requireEntity(protoType);

    LOG.debug("createResourceWithoutGivenID: protoType=[{}]", protoType);
    LOG.debug("annotations: [{}]", protoType.getAnnotations());

    final AlexandriaResource resource = new AlexandriaResource(protoType);
    final String id = resource.getId().toString();
    return Response.created(URI.create(id)).entity(entityBuilder.build(resource)).build();
  }

  @PUT
  @Path("{uuid}")
  public Response createResourceAtSpecificID(@PathParam("uuid") final UUIDParam paramId, AlexandriaResource protoType) {
    requireEntity(protoType);
    requireCompatibleIds(paramId, protoType::getId);

    LOG.debug("createResourceAtSpecificID: paramId=[{}] vs protoType.id=[{}]", paramId, protoType.getId());

    resourceService.createResource(protoType);

    return Response.created(HERE).build();
  }

  @Path("{uuid}/annotations")
  public ResourceAnnotations getAnnotationsForResource(@PathParam("uuid") final UUIDParam uuidParam) {
    return annotationsFor(uuidParam.getValue());
  }

  @GET
  @Path("{uuid}/ref")
  public Response getResourceRef(@PathParam("uuid") final UUIDParam uuidParam) {
    AlexandriaResource resource = resourceService.readResource(uuidParam.getValue());
    return Response.ok(RefEntity.of(resource.getRef())).build();
  }

  private ResourceAnnotations annotationsFor(UUID uuid) {
    return new ResourceAnnotations(resourceService, uuid);
  }

}
