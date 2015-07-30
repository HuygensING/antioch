package nl.knaw.huygens.alexandria.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ConflictException extends WebApplicationException {
  private static final long serialVersionUID = 1L;
  static final ErrorEntity DEFAULT_ENTITY = ErrorEntityBuilder.build("");

  public ConflictException() {
    super(responseWithErrorEntity(DEFAULT_ENTITY));
  }

  public ConflictException(String message) {
    super(responseWithErrorEntity(ErrorEntityBuilder.build(message)));
  }

  private static Response responseWithErrorEntity(ErrorEntity errorEntity) {
    return Response.status(Status.CONFLICT).entity(errorEntity).build();
  }

}
