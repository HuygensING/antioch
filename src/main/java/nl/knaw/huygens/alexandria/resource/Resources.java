package nl.knaw.huygens.alexandria.resource;

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
import javax.ws.rs.core.Response.Status;
import java.net.URI;

import nl.knaw.huygens.alexandria.exception.IllegalResourceException;
import nl.knaw.huygens.alexandria.exception.ResourceExistsException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.ResourceService;

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
    try {
      return Response.ok(resourceService.getResource(uuid)).build();
    } catch (IllegalResourceException e) {
      return Response.status(Status.BAD_REQUEST).entity("Malformed UUID: " + uuid).build();
    }
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createResourceWithoutGivenID(AlexandriaResource protoType) {
    System.err.println("createResourceWithoutGivenID: protoType=" + protoType);
    System.err.println("annotations: " + protoType.getAnnotations());
    final AlexandriaResource res = new AlexandriaResource(protoType);//resourceService.createResource(protoType);
    final String id = res.getId().toString();
    return Response.created(URI.create(id)).entity(res).build();
  }

  @PUT
  @Path("/{uuid}")
  public Response createResourceAtSpecificID(@PathParam("uuid") final String uuid, AlexandriaResource protoType) {
    System.err.println("createResourceAtSpecificID: uuid=" + uuid + " vs protoType.id=" + protoType.getId());

    try {
      resourceService.createResource(protoType);
      return Response.created(HERE).build();
    } catch (IllegalResourceException e) {
      // TODO: improve by making parameter uuid of type UUID and adding a Jersey level converter
      return Response.status(Status.BAD_REQUEST).entity("Malformed UUID: " + uuid).build();
    } catch (ResourceExistsException e) {
      return Response.status(Status.CONFLICT).entity(protoType).build();
    }
  }

}
