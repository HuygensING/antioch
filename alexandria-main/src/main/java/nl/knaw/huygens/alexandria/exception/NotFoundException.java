package nl.knaw.huygens.alexandria.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class NotFoundException extends WebApplicationException {
  private static final long serialVersionUID = 1L;
  static final ErrorEntity DEFAULT_ENTITY = ErrorEntityBuilder.build("Not Found");

  public NotFoundException() {
    super(Response.status(Status.NOT_FOUND).entity(DEFAULT_ENTITY).build());
  }

  public NotFoundException(String message) {
    super(Response.status(Status.NOT_FOUND).entity(ErrorEntityBuilder.build(message)).build());
  }
}
