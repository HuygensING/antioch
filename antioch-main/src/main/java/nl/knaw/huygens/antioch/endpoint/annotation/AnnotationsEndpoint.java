package nl.knaw.huygens.antioch.endpoint.annotation;

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

import java.util.UUID;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.api.model.StatePrototype;
import nl.knaw.huygens.antioch.endpoint.JSONEndpoint;
import nl.knaw.huygens.antioch.endpoint.UUIDParam;
import nl.knaw.huygens.antioch.exception.ConflictException;
import nl.knaw.huygens.antioch.exception.NotFoundException;
import nl.knaw.huygens.antioch.exception.TentativeObjectException;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.service.AntiochService;

@Path(EndpointPaths.ANNOTATIONS)
@Api("annotations")
public class AnnotationsEndpoint extends JSONEndpoint {
  private final AntiochService service;
  private final AnnotationEntityBuilder entityBuilder;
  private final AnnotationDeprecationRequestBuilder requestBuilder;

  @Inject
  public AnnotationsEndpoint(AntiochService service, //
                             AnnotationEntityBuilder entityBuilder, //
                             AnnotationDeprecationRequestBuilder requestBuilder) {
    this.service = service;
    this.entityBuilder = entityBuilder;
    this.requestBuilder = requestBuilder;
  }

  static Supplier<NotFoundException> annotationNotFoundForId(Object id) {
    return () -> new NotFoundException(NoAnnotationFoundWithId(id));
  }

  private static String NoAnnotationFoundWithId(Object id) {
    return "No annotation found with id " + id;
  }

  private static Supplier<NotFoundException> annotationNotFoundForIdAndRevision(Object id, Integer revision) {
    return () -> new NotFoundException(NoAnnotationFoundWithId(id) + ", revision " + revision);
  }

  static WebApplicationException annotationIsTentative(UUID uuid) {
    return new TentativeObjectException("annotation " + uuid + " is tentative, please confirm it first");
  }

  @GET
  @Path("{uuid}")
  @ApiOperation(value = "get the annotation", response = AnnotationEntity.class)
  public Response readAnnotation(@PathParam("uuid") UUIDParam uuidParam) {
    return ok(readExistingAnnotation(uuidParam));
  }

  // Sub-resource delegation

  @GET
  @Path("{uuid}/" + EndpointPaths.REV + "/{revision}")
  @ApiOperation(value = "get the given revision of the annotation", response = AnnotationEntity.class)
  public Response readVersionedAnnotation(@PathParam("uuid") UUIDParam uuidParam, //
                                          @PathParam("revision") Integer revision) {
    return ok(readExistingAnnotationWithIdAndRevision(uuidParam, revision));
  }

  @PUT
  @Path("{uuid}")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value //
      = "make a new annotation from the payload and use it to deprecate the annotation with the given uuid")
  public Response deprecateAnnotation(@PathParam("uuid") UUIDParam uuidParam, //
                                      @NotNull @OmittedOrMatchingType AnnotationPrototype prototype) {
    AntiochAnnotation annotation = readExistingAnnotation(uuidParam);
    prototype.setState(AntiochState.CONFIRMED);
    AnnotationDeprecationRequest request = requestBuilder.ofAnnotation(annotation).build(prototype);
    request.execute(service);
    return noContent();
  }

  @DELETE
  @Path("{uuid}")
  public Response deleteAnnotation(@PathParam("uuid") final UUIDParam uuidParam) {
    AntiochAnnotation annotation = readExistingAnnotation(uuidParam);
    if (!annotation.getAnnotations().isEmpty()) {
      throw new ConflictException("annotation " + annotation.getId() + " still has annotations");
    }

    service.deleteAnnotation(annotation);

    return noContent();
  }

  @PUT
  @Path("{uuid}/state")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "update the state of the annotation (only state=CONFIRMED accepted)")
  public Response setAnnotationState(@PathParam("uuid") final UUIDParam uuidParam, @NotNull StatePrototype protoType) {
    // Log.trace("protoType=[{}]", protoType);
    AntiochAnnotation annotation = readExistingAnnotation(uuidParam);
    if (protoType.isConfirmed()) {
      if (!annotation.isActive()) {
        throw new ConflictException(annotation.getState() + " annotations cannot be set to CONFIRMED");
      }
      service.confirmAnnotation(annotation.getId());
      return noContent();
    }
    throw new ConflictException("Annotations can only be CONFIRMED via their /state endpoint");
  }

  @Path("{uuid}/annotations")
  public Class<AnnotationAnnotationsEndpoint> getAnnotations() {
    return AnnotationAnnotationsEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

  @Path("{uuid}/provenance")
  public Class<AnnotationProvenanceEndpoint> getProvenance() {
    return AnnotationProvenanceEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

  private Response ok(AntiochAnnotation annotation) {
    return ok(entityBuilder.build(annotation));
  }

  private AntiochAnnotation readExistingAnnotation(UUIDParam uuidParam) {
    return service.readAnnotation(uuidParam.getValue()) //
        .orElseThrow(annotationNotFoundForId(uuidParam));
  }

  private AntiochAnnotation readExistingAnnotationWithIdAndRevision(UUIDParam uuidParam, int revision) {
    return service.readAnnotation(uuidParam.getValue(), revision) //
        .orElseThrow(annotationNotFoundForIdAndRevision(uuidParam, revision));
  }

}
