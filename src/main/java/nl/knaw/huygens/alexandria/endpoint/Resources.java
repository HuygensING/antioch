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

@Path("/resources")
public class Resources extends JSONEndpoint {
  public static final URI HERE = URI.create("");

  private final ResourceService resourceService;

  public Resources(@Context ResourceService resourceService) {
    this.resourceService = resourceService;
  }

  @GET
  @Path("/{uuid}")
  public Response getResourceByID(@PathParam("uuid") final UUIDParam uuid) {
    return Response.ok(resourceService.getResource(uuid.getValue())).build();
  }

  @POST
  public Response createResourceWithoutGivenID(AlexandriaResource protoType) {
    requireValidEntity(protoType);

    System.err.println("createResourceWithoutGivenID: protoType=" + protoType);
    System.err.println("annotations: " + protoType.getAnnotations());

    final AlexandriaResource res = new AlexandriaResource(protoType);//resourceService.createResource(protoType);
    final String id = res.getId().toString();
    return Response.created(URI.create(id)).entity(res).build();
  }

  @PUT
  @Path("/{uuid}")
  public Response createResourceAtSpecificID(@PathParam("uuid") final UUIDParam paramId, AlexandriaResource protoType) {
    requireValidEntity(protoType);
    requireCompatibleIds(paramId, protoType::getId);

    System.err.println("createResourceAtSpecificID: paramId=" + paramId + " vs protoType.id=" + protoType.getId());

    resourceService.createResource(protoType);

    return Response.created(HERE).build();
  }

}
