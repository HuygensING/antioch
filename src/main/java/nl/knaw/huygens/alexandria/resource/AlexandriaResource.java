package nl.knaw.huygens.alexandria.resource;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.UUID;

import nl.knaw.huygens.alexandria.InMemoryReferenceStore;
import nl.knaw.huygens.alexandria.external.IllegalReferenceException;
import nl.knaw.huygens.alexandria.external.ReferenceExistsException;
import nl.knaw.huygens.alexandria.service.ReferenceService;

@Path("/resources")
public class AlexandriaResource {
  public static final URI HERE = URI.create("");

  private final ReferenceService referenceService = new ReferenceService(new InMemoryReferenceStore());

  @POST
  public Response createResourceWithoutGivenID(String body) {
    final UUID uuid = referenceService.createReference(body);
    return Response.created(URI.create(uuid.toString())).build();
  }

  @PUT
  @Path("/{uuid}")
  public Response createResourceAtSpecificID(@PathParam("uuid") final String uuid, String body) {
    try {
      referenceService.createReference(uuid, body);
      return Response.created(HERE).build();
    } catch (IllegalReferenceException e) {
      // TODO: improve by making parameter uuid of type UUID and adding a Jersey level converter
      return Response.status(Status.BAD_REQUEST).entity("Malformed UUID: " + uuid).build();
    } catch (ReferenceExistsException e) {
      return Response.status(Status.CONFLICT).entity(body).build();
    }
  }

}
