package nl.knaw.huygens.alexandria.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;

import nl.knaw.huygens.alexandria.endpoint.param.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/resources")
public class Resources extends JSONEndpoint {
  private static final URI HERE = URI.create("");

  private static final Logger LOG = LoggerFactory.getLogger(Resources.class);

  private final ResourceService resourceService;

  public Resources(@Context ResourceService resourceService) {
    LOG.trace("Resources created, resourceService=[{}]", resourceService);
    this.resourceService = resourceService;
  }

  @GET
  @Path("/{uuid}")
  public Response getResourceByID(@PathParam("uuid") final UUIDParam uuid) {
    return Response.ok(resourceService.readResource(uuid.getValue())).build();
  }

  @POST
  public Response createResourceWithoutGivenID(AlexandriaResource protoType) {
    requireValidEntity(protoType);

    LOG.debug("createResourceWithoutGivenID: protoType=[{}]", protoType);
    LOG.debug("annotations: [{}]", protoType.getAnnotations());

    final AlexandriaResource res = new AlexandriaResource(protoType);//resourceService.createResource(protoType);
    final String id = res.getId().toString();
    return Response.created(URI.create(id)).entity(res).build();
  }

  @PUT
  @Path("/{uuid}")
  public Response createResourceAtSpecificID(@PathParam("uuid") final UUIDParam paramId, AlexandriaResource protoType) {
    requireValidEntity(protoType);
    requireCompatibleIds(paramId, protoType::getId);

    LOG.debug("createResourceAtSpecificID: paramId=[{}] vs protoType.id=[{}]", paramId, protoType.getId());

    resourceService.createResource(protoType);

    return Response.created(HERE).build();
  }

  @Path("/{uuid}/annotations")
  public ResourceAnnotations getAnnotationsForResource(@PathParam("uuid") final UUIDParam uuidParam) {
    return new ResourceAnnotations(resourceService, uuidParam.getValue());
  }

  @GET
  @Path("/{uuid}/ref")
  public Response getResourceRef(@PathParam("uuid") final UUIDParam uuidParam) {
    final String ref = resourceService.readResource(uuidParam.getValue()).getRef();
    return Response.ok(new RefWrapper(ref)).build();
  }

  static class RefWrapper {
    private final String ref;

    public RefWrapper(String ref) {
      this.ref = ref;
    }

    public String getRef() {
      return ref;
    }
  }
}
