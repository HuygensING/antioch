package nl.knaw.huygens.alexandria.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class MissingEntityException extends WebApplicationException {
  public MissingEntityException() {
    super(Response.status(Status.BAD_REQUEST).entity("Missing entity (empty request body)").build());
  }
}
