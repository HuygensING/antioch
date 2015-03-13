package nl.knaw.huygens.alexandria.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class IllegalResourceException extends WebApplicationException {
  public IllegalResourceException(Response response) {
    super(response);
  }

  public IllegalResourceException(String msg) {
    super(Response.status(Status.CONFLICT).entity(msg).build());
  }
}
