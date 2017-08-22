package nl.knaw.huygens.alexandria.jersey.exceptionmappers;

/*
 * #%L
 * alexandria-service
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

import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.huygens.alexandria.exception.ErrorEntityBuilder;
import nl.knaw.huygens.alexandria.query.AlexandriaQueryParseException;

@Singleton
@Provider
public class AlexandriaQueryParseExceptionMapper implements ExceptionMapper<AlexandriaQueryParseException> {
  private final static Logger LOG = LoggerFactory.getLogger(AlexandriaQueryParseExceptionMapper.class);

  @Override
  public Response toResponse(AlexandriaQueryParseException e) {
    LOG.error("error:{}", e);
    return Response//
        .status(Status.BAD_REQUEST).entity(ErrorEntityBuilder.build(e)).build();
  }

}
