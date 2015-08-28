package nl.knaw.huygens.alexandria.endpoint;

import java.net.URI;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

//rfc4627: JSON text SHALL be encoded in Unicode. The default encoding is UTF-8.
@Produces(MediaType.APPLICATION_JSON)
public abstract class JSONEndpoint {
  protected Response created(URI location) {
    return Response.created(location).build();
  }

  protected Response methodNotImplemented() {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  protected Response noContent() {
    return Response.noContent().build();
  }

  protected Response ok() {
    return Response.ok().build();
  }

  protected Response ok(Object entity) {
    return Response.ok(entity).build();
  }
}
