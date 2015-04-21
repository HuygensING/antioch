package nl.knaw.huygens.alexandria.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.UUID;

public class IdMismatchException extends WebApplicationException {
  public IdMismatchException(UUID paramId, UUID entityId) {
    super(Response.status(Status.CONFLICT)
                  .entity(String.format("Path id .../%s does not match entity id %s", paramId, entityId)).build());
  }
}
