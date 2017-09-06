package nl.knaw.huygens.antioch.endpoint;

/*
 * #%L
 * antioch-main
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

import java.net.URI;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

//rfc4627: JSON text SHALL be encoded in Unicode. The default encoding is UTF-8.
@Produces(MediaType.APPLICATION_JSON)
public abstract class JSONEndpoint {
  protected Response created(URI location) {
    return Response.created(location).build();
  }

  protected Response methodNotImplemented() {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  protected Response noContent() {
    return Response.noContent().build();
  }

  protected Response conflict() {
    return Response.status(Status.CONFLICT).build();
  }

  protected Response ok() {
    return Response.ok().build();
  }

  protected Response ok(Object entity) {
    return Response.ok(entity).build();
  }

  protected Response notImplemented() {
    return Response.status(Status.NOT_IMPLEMENTED).build();
  }

  protected Response notImplemented(Object entity) {
    return Response.status(Status.NOT_IMPLEMENTED).entity(entity).build();
  }

}
