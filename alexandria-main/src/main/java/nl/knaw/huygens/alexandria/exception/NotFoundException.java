package nl.knaw.huygens.alexandria.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class NotFoundException extends WebApplicationException {
  public NotFoundException() {
    super(Response.status(Status.NOT_FOUND).build());
  }

  public NotFoundException(String message) {
    super(Response.status(Status.NOT_FOUND).entity(message).build());
  }
}
