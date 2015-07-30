package nl.knaw.huygens.alexandria.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class BadRequestException extends WebApplicationException {
  private static final long serialVersionUID = 1L;

  public BadRequestException(String message) {
    super(Response.status(Status.BAD_REQUEST).entity(ErrorEntityBuilder.build(message)).build());
  }
}
