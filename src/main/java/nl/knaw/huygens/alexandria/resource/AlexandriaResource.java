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
import nl.knaw.huygens.alexandria.reference.ReferenceExistsException;
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
  @Path("/{id}")
  public Response createResourceAtSpecificID(@PathParam("id") final String id, String body) {
    final UUID uuid;
    try {
      uuid = UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      return Response.status(Status.BAD_REQUEST).entity("Not a valid UUID: " + id).build();
    }
    
    if (resourceBelongsToID(body, id)) {
      try {
        referenceService.createReference(uuid, body);
      } catch (ReferenceExistsException e) {
        return Response.status(Status.CONFLICT).entity(body).build();
      }
      return Response.created(HERE).build();
    }

    return Response.status(Status.BAD_REQUEST).entity("Id '" + id + "' not present in body").build();
  }

  // TODO: JSONify
  private boolean resourceBelongsToID(String body, String id) {
    return body.contains(id);
  }
}
