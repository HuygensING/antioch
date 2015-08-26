package nl.knaw.huygens.alexandria.endpoint.annotation;

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

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.StatePrototype;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.exception.ConflictException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.exception.TentativeObjectException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Path(EndpointPaths.ANNOTATIONS)
@Api("annotations")
public class AnnotationsEndpoint extends JSONEndpoint {
  public final static String REVPATH = "/rev/";

  private final AlexandriaService service;
  private final AnnotationEntityBuilder entityBuilder;
  private final LocationBuilder locationBuilder;
  private final AnnotationDeprecationRequestBuilder requestBuilder;

  @Inject
  public AnnotationsEndpoint(AlexandriaService service, //
                             AnnotationEntityBuilder entityBuilder, //
                             AnnotationDeprecationRequestBuilder requestBuilder, //
                             LocationBuilder locationBuilder) {
    this.service = service;
    this.entityBuilder = entityBuilder;
    this.requestBuilder = requestBuilder;
    this.locationBuilder = locationBuilder;
  }

  public static Supplier<NotFoundException> annotationNotFoundForId(Object id) {
    return () -> new NotFoundException(NoAnnotationFoundWithId(id));
  }

  private static String NoAnnotationFoundWithId(Object id) {
    return "No annotation found with id " + id;
  }

  // @POST
  // @Path("{uuid}/deprecation")
  // @Consumes(MediaType.APPLICATION_JSON)
  // @ApiOperation(value = "make a new annotation from the payload and use it to deprecate the annotation with the
  // given uuid")
  // public Response deprecateAnnotation(@PathParam("uuid") UUIDParam uuidParam, AnnotationPrototype prototype) {
  // UUID uuid = uuidParam.getValue();
  // prototype.setState(AlexandriaState.TENTATIVE);
  // AnnotationDeprecationRequest request = requestBuilder.ofAnnotation(uuid).build(prototype);
  // AlexandriaAnnotation newAnnotation = request.execute(service);
  // return Response.created(locationBuilder.locationOf(newAnnotation)).build();
  // }

  public static Supplier<NotFoundException> annotationNotFoundForIdAndRevision(Object id, Integer revision) {
    return () -> new NotFoundException(NoAnnotationFoundWithId(id) + ", revision " + revision);
  }

  public static WebApplicationException annotationIsTentative(UUID uuid) {
    return new TentativeObjectException("annotation " + uuid + " is tentative, please confirm it first");
  }

  @GET
  @Path("{uuid}")
  @ApiOperation(value = "get the annotation", response = AnnotationEntity.class)
  public Response readAnnotation(@PathParam("uuid") UUIDParam uuidParam) {
    final AlexandriaAnnotation annotation = service.readAnnotation(uuidParam.getValue())//
        .orElseThrow(annotationNotFoundForId(uuidParam));
    return Response.ok(entityBuilder.build(annotation)).build();
  }

  // Sub-resource delegation

  @GET
  @Path("{uuid}" + REVPATH + "{revision}")
  @ApiOperation(value = "get the given revision of the annotation", response = AnnotationEntity.class)
  public Response readVersionedAnnotation(@PathParam("uuid") UUIDParam uuidParam, //
                                          @PathParam("revision") Integer revision) {
    final AlexandriaAnnotation annotation = service.readAnnotation(uuidParam.getValue(), revision)//
        .orElseThrow(annotationNotFoundForIdAndRevision(uuidParam, revision));
    return Response.ok(entityBuilder.build(annotation)).build();
  }

  @PUT
  @Path("{uuid}")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value //
      = "make a new annotation from the payload and use it to deprecate the annotation with the given uuid")
  public Response deprecateAnnotation(@PathParam("uuid") UUIDParam uuidParam, //
                                      @NotNull @OmittedOrMatchingType AnnotationPrototype prototype) {
    UUID uuid = uuidParam.getValue();
    final AlexandriaAnnotation annotation = service.readAnnotation(uuid)//
        .orElseThrow(annotationNotFoundForId(uuidParam));
    prototype.setState(AlexandriaState.CONFIRMED);
    AnnotationDeprecationRequest request = requestBuilder.ofAnnotation(annotation).build(prototype);
    request.execute(service);
    return Response.noContent().build();
  }

  @DELETE
  @Path("{uuid}")
  public Response deleteAnnotation(@PathParam("uuid") final UUIDParam uuidParam) {
    UUID uuid = uuidParam.getValue();
    final AlexandriaAnnotation annotation = service.readAnnotation(uuid)//
        .orElseThrow(annotationNotFoundForId(uuidParam));
    if (!annotation.getAnnotations().isEmpty()) {
      throw new ConflictException("annotation " + uuid + " still has annotations");
    }

    service.deleteAnnotation(annotation);

    return Response.noContent().build();
  }

  @PUT
  @Path("{uuid}/state")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "update the state of the annotation (only state=CONFIRMED accepted for now)")
  public Response setAnnotationState(@PathParam("uuid") final UUIDParam uuidParam, @NotNull StatePrototype protoType) {
    Log.trace("protoType=[{}]", protoType);
    UUID id = uuidParam.getValue();
    AlexandriaAnnotation annotation = service.readAnnotation(id)//
        .orElseThrow(annotationNotFoundForId(uuidParam));
    if (protoType.isConfirmed()) {
      if (!annotation.isActive()) {
        throw new ConflictException(annotation.getState() + " annotations cannot be set to CONFIRMED");
      }
      service.confirmAnnotation(id);
      return Response.noContent().build();
    }
    throw new BadRequestException("for now, you can only set the state to CONFIRMED");
  }

  @Path("{uuid}/annotations")
  public Class<AnnotationAnnotationsEndpoint> getAnnotations() {
    return AnnotationAnnotationsEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

  @Path("{uuid}/provenance")
  public Class<AnnotationProvenanceEndpoint> getProvenance() {
    return AnnotationProvenanceEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

}
