package nl.knaw.huygens.alexandria.endpoint.annotationbody;

import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Path(EndpointPaths.ANNOTATIONBODIES)
@Api(EndpointPaths.ANNOTATIONBODIES)
public class AnnotationBodiesEndpoint extends JSONEndpoint {
  private final AlexandriaService service;
  private final AnnotationBodyCreationRequestBuilder requestBuilder;

  @Inject
  public AnnotationBodiesEndpoint(AlexandriaService service, //
      AnnotationBodyCreationRequestBuilder requestBuilder //
  ) {
    this.service = service;
    this.requestBuilder = requestBuilder;
  }

  // @GET
  // @Path("{uuid}")
  // @ApiOperation(value = "get the annotation body", response = AnnotationBodyEntity.class)
  // public Response readAnnotationBody(@PathParam("uuid") UUIDParam uuidParam) {
  // final AlexandriaAnnotationBody annotationBody = service.readAnnotationBody(uuidParam.getValue())//
  // .orElseThrow(() -> new NotFoundException("No annotationbody found with id " + uuidParam));
  // return ok(entityBuilder.build(annotationBody));
  // }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("create a new annotation body")
  public Response createAnnotationBody(final AnnotationBodyPrototype prototype) {
    final AnnotationBodyCreationRequest request = requestBuilder.build(prototype);
    request.execute(service);

    if (request.wasCreated()) {
      return created(locationOf(prototype.getId().getValue()));
    }

    return conflict();
  }

  @DELETE
  @Path("{uuid}")
  public Response deleteNotSupported(@PathParam("uuid") final UUIDParam paramId) {
    return methodNotImplemented();
  }

  private URI locationOf(UUID uuid) {
    return URI.create(String.format("%s/%s", EndpointPaths.ANNOTATIONBODIES + "/", uuid));
  }

}
