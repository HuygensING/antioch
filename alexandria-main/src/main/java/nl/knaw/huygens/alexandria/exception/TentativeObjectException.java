package nl.knaw.huygens.alexandria.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class TentativeObjectException extends WebApplicationException {
  private static final long serialVersionUID = 1L;
  static final ErrorEntity DEFAULT_ENTITY = ErrorEntityBuilder.build("Object is tentative");

  public TentativeObjectException() {
    super(responseWithErrorEntity(DEFAULT_ENTITY));
  }

  public TentativeObjectException(String message) {
    super(responseWithErrorEntity(ErrorEntityBuilder.build(message)));
  }

  // CONFLICT or BADREQUEST?
  private static Response responseWithErrorEntity(ErrorEntity errorEntity) {
    return Response.status(Status.CONFLICT).entity(errorEntity).build();
  }

}
