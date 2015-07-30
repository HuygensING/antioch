package nl.knaw.huygens.alexandria.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class IllegalResourceException extends WebApplicationException {
  private static final long serialVersionUID = 1L;

  public IllegalResourceException(String message) {
    super(Response.status(Status.CONFLICT).entity(ErrorEntityBuilder.build(message)).build());
  }
}
