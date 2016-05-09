package nl.knaw.huygens.alexandria.endpoint.annotationbody;

import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Path(EndpointPaths.ANNOTATIONBODIES)
@Api(EndpointPaths.ANNOTATIONBODIES)
public class AnnotationBodiesEndpoint extends JSONEndpoint {
  private final AlexandriaService service;
  private final AnnotationBodyEntityBuilder entityBuilder;
  private final AnnotationBodyCreationRequestBuilder requestBuilder;

  @Inject
  public AnnotationBodiesEndpoint(AlexandriaService service, //
      AnnotationBodyCreationRequestBuilder requestBuilder, //
      AnnotationBodyEntityBuilder entityBuilder) {
    this.service = service;
    this.requestBuilder = requestBuilder;
    this.entityBuilder = entityBuilder;
  }

  @GET
  @Path("{uuid}")
  @ApiOperation(value = "get the annotation body", response = AnnotationBodyEntity.class)
  public Response readAnnotationBody(@PathParam("uuid") UUIDParam uuidParam) {
    final AlexandriaAnnotationBody annotationBody = service.readAnnotationBody(uuidParam.getValue())//
        .orElseThrow(() -> new NotFoundException("No annotationbody found with id " + uuidParam));
    return ok(entityBuilder.build(annotationBody));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("create a new annotation body")
  public Response createAnnotationBody(final AnnotationBodyPrototype prototype) {
    final AnnotationBodyCreationRequest request = requestBuilder.build(prototype);
    request.execute(service);

    if (request.wasCreated()) {
      return created(locationOf(prototype.getId().getValue()));
    }

    return Response.status(Status.CONFLICT).build();
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
