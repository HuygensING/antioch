package nl.knaw.huygens.alexandria.endpoint;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;

@Singleton
@Path("e")
public class ExceptionEndpoint extends JSONEndpoint {

  @GET
  @Path("rte")
  public Response throwRuntimeException() {
    throw new RuntimeException("RuntimeException");
  }

  @GET
  @Path("nfe")
  public Response throwNotFoundException() {
    throw new NotFoundException("Object not found");
  }

  @GET
  @Path("bre")
  public Response throwBadRequestException() {
    throw new BadRequestException("Bad request");
  }
}
