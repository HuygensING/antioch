package nl.knaw.huygens.alexandria.jersey.exceptionmappers;

import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import nl.knaw.huygens.alexandria.exception.ErrorEntityBuilder;

@Singleton
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

  @Override
  public Response toResponse(NotFoundException e) {
    return Response.status(Status.BAD_REQUEST).entity(ErrorEntityBuilder.build("No such endpoint")).build();
  }

}
