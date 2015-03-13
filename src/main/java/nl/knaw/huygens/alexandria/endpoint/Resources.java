package nl.knaw.huygens.alexandria.endpoint;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

import nl.knaw.huygens.alexandria.exception.IdMismatchException;
import nl.knaw.huygens.alexandria.exception.MissingEntityException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.ResourceService;
import nl.knaw.huygens.alexandria.util.UUIDParam;

@Path("/resources")
public class Resources {
  public static final URI HERE = URI.create("");

  private final ResourceService resourceService;

  public Resources(@Context ResourceService resourceService) {
    this.resourceService = resourceService;
  }

  @GET
  @Path("/{uuid}")
  public Response getResourceByID(@PathParam("uuid") final String uuid) {
    return Response.ok(resourceService.getResource(uuid)).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createResourceWithoutGivenID(AlexandriaResource protoType) {
    System.err.println("createResourceWithoutGivenID: protoType=" + protoType);
    System.err.println("annotations: " + protoType.getAnnotations());
    final AlexandriaResource res = new AlexandriaResource(protoType);//resourceService.createResource(protoType);
    final String id = res.getId().toString();
    return Response.created(URI.create(id)).entity(res).build();
  }

  @PUT
  @Path("/{uuid}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createResourceAtSpecificID(@PathParam("uuid") final UUIDParam paramId, AlexandriaResource protoType) {
    Optional.ofNullable(protoType).orElseThrow(MissingEntityException::new);

    System.err.println("createResourceAtSpecificID: paramId=" + paramId + " vs protoType.id=" + protoType.getId());

    Optional.ofNullable(protoType.getId()).ifPresent(protoId -> {
      if (!protoId.equals(paramId.getValue())) {
        throw new IdMismatchException(paramId.getValue(), protoType.getId());
      }
    });

    resourceService.createResource(protoType);

    return Response.created(HERE).build();
  }

}
