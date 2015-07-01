package nl.knaw.huygens.alexandria.jersey.exceptionmappers;

import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.exception.ErrorEntityBuilder;

@Singleton
@Provider
public class AnyExceptionMapper implements ExceptionMapper<Exception> {

  @Override
  public Response toResponse(Exception e) {
    Log.error("error:{}", e);
    return Response//
        .status(Status.INTERNAL_SERVER_ERROR).entity(ErrorEntityBuilder.build(e)).build();
  }

}
