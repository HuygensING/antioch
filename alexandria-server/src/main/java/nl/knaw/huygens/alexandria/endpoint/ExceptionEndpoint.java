package nl.knaw.huygens.alexandria.endpoint;

/*
 * #%L
 * alexandria-server
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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
