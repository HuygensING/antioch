package nl.knaw.huygens.alexandria.jersey.exceptionmappers;

import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.exception.ErrorEntityBuilder;
import nl.knaw.huygens.alexandria.query.AlexandriaQueryParseException;

@Singleton
@Provider
public class AlexandriaQueryParseExceptionMapper implements ExceptionMapper<AlexandriaQueryParseException> {

  @Override
  public Response toResponse(AlexandriaQueryParseException e) {
    Log.error("error:{}", e);
    return Response//
        .status(Status.BAD_REQUEST).entity(ErrorEntityBuilder.build(e)).build();
  }

}
