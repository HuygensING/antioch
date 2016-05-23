package nl.knaw.huygens.alexandria.exception;

/*
 * #%L
 * alexandria-main
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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.alexandria.api.model.ErrorEntity;

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
