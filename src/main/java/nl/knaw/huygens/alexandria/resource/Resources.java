package nl.knaw.huygens.alexandria.resource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.UUID;

import nl.knaw.huygens.alexandria.external.IllegalResourceException;
import nl.knaw.huygens.alexandria.external.ResourceExistsException;
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
  public Response createResourceWithoutGivenID(String body) {
    System.err.println("createResourceWithoutGivenID: referenceService=" + resourceService);
    final UUID uuid = resourceService.createResource(body);
    return Response.created(URI.create(uuid.toString())).build();
  }

  @PUT
  @Path("/{uuid}")
  public Response createResourceAtSpecificID(@PathParam("uuid") final String uuid, String body) {
    System.err.println("createResourceAtSpecificID: referenceService=" + resourceService);
    try {
      resourceService.createResource(uuid, body);
      return Response.created(HERE).build();
    } catch (IllegalResourceException e) {
      // TODO: improve by making parameter uuid of type UUID and adding a Jersey level converter
      return Response.status(Status.BAD_REQUEST).entity("Malformed UUID: " + uuid).build();
    } catch (ResourceExistsException e) {
      return Response.status(Status.CONFLICT).entity(body).build();
    }
  }

}
