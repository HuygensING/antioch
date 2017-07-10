package nl.knaw.huygens.alexandria.exception;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.alexandria.api.model.ErrorEntity;

public class NotFoundException extends WebApplicationException {
  private static final long serialVersionUID = 1L;
  static final ErrorEntity DEFAULT_ENTITY = ErrorEntityBuilder.build("Not Found");

  public NotFoundException() {
    super(Response.status(Status.NOT_FOUND).entity(DEFAULT_ENTITY).build());
  }

  public NotFoundException(String message) {
    super(Response.status(Status.NOT_FOUND).entity(ErrorEntityBuilder.build(message)).type(MediaType.APPLICATION_JSON).build());
  }
}
