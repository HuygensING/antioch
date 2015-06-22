package nl.knaw.huygens.alexandria.endpoint.annotation;

import io.swagger.annotations.Api;

import java.util.Optional;

import javax.inject.Inject;
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
  public Response readAnnotation(@PathParam("uuid") UUIDParam uuidParam) {
    Optional<AlexandriaAnnotation> optionalAnnotation = service.readAnnotation(uuidParam.getValue());
    if (optionalAnnotation.isPresent()) {
      final AlexandriaAnnotation annotation = optionalAnnotation.get();
      final AnnotationEntity entity = entityBuilder.build(annotation);
      return Response.ok(entity).build();

    } else {
      throw new NotFoundException("No annotation found with id " + uuidParam);
    }
  }

  @DELETE
  @Path("{uuid}")
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
}
