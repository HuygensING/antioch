package nl.knaw.huygens.alexandria.endpoint.annotation;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Path(EndpointPaths.ANNOTATIONS)
@Api("annotations")
public class AnnotationsEndpoint extends JSONEndpoint {
  private final AlexandriaService service;
  private final AnnotationEntityBuilder entityBuilder;

  @Inject
  public AnnotationsEndpoint(AlexandriaService service, //
      AnnotationEntityBuilder entityBuilder) {
    this.service = service;
    this.entityBuilder = entityBuilder;
  }

  @GET
  @Path("{uuid}")
  @Consumes()
  @ApiOperation(value = "get the annotation", response = AnnotationEntity.class)
  public Response readAnnotation(@PathParam("uuid") UUIDParam uuidParam) {
    final AlexandriaAnnotation annotation = service.readAnnotation(uuidParam.getValue())//
        .orElseThrow(annotationNotFoundForId(uuidParam));
    return Response.ok(entityBuilder.build(annotation)).build();
  }

  @DELETE
  @Path("{uuid}")
  @Consumes()
  public Response deleteNotSupported(@PathParam("uuid") final UUIDParam paramId) {
    return methodNotImplemented();
  }

  // Sub-resource delegation

  @Path("{uuid}/annotations")
  public Class<AnnotationAnnotationsEndpoint> getAnnotations() {
    return AnnotationAnnotationsEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

  @Path("{uuid}/provenance")
  public Class<AnnotationProvenanceEndpoint> getProvenance() {
    return AnnotationProvenanceEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

  public static Supplier<NotFoundException> annotationNotFoundForId(Object id) {
    return () -> new NotFoundException("No annotation found with id " + id);
  };

}
