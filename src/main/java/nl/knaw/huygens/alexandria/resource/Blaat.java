package nl.knaw.huygens.alexandria.resource;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

@Path("/resources")
public class Blaat {
  @POST
  public Response postIt() {
    return Response.created(URI.create(UUID.randomUUID().toString())).build();
  }
}
