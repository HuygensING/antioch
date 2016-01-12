package nl.knaw.huygens.alexandria.jersey.exceptionmappers;

/*
 * #%L
 * alexandria-service
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
