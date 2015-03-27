package nl.knaw.huygens.alexandria.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

import nl.knaw.huygens.alexandria.AnnotationCreationParameters;
import nl.knaw.huygens.alexandria.AnnotationCreationRequest;
import nl.knaw.huygens.alexandria.endpoint.param.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AnnotationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(Annotations.ANNOTATIONS_PATH)
public class Annotations extends JSONEndpoint {
  static final String ANNOTATIONS_PATH = "annotations";

  private static final Logger LOG = LoggerFactory.getLogger(Annotations.class);

  private final AnnotationService service;
  private final AnnotationEntityBuilder entityBuilder;
  private final AnnotationRequestValidator validator;

  public Annotations(@Context AnnotationService service, //
                     @Context AnnotationRequestValidator validator, //
                     @Context AnnotationEntityBuilder entityBuilder) {
    this.service = service;
    this.validator = validator;
    this.entityBuilder = entityBuilder;
  }

  @GET
  @Path("/{uuid}")
  public Response readAnnotation(@PathParam("uuid") UUIDParam uuidParam) {
    final AlexandriaAnnotation annotation = service.readAnnotation(uuidParam.getValue());
    final AnnotationEntity entity = entityBuilder.build(annotation);
    return Response.ok(entity).build();
  }

  @POST
  public Response createAnnotation(final AnnotationCreationParameters suspiciousParams) {
    LOG.debug("suspiciousParams: [{}]", suspiciousParams.getAnnotations()); // FIXME: WHY IS THIS EMPTY IN THE TEST?
    // Added 'annotations' in Annotation.html, but now we get 400 Bad Request???
    final AnnotationCreationRequest validatedRequest = validator.validate(suspiciousParams);
    LOG.debug("Stream of annotations:");
    validatedRequest.streamAnnotations().forEach(this::printAnno);
    final AlexandriaAnnotation annotation = service.createAnnotation(validatedRequest);
    final AnnotationEntity entity = entityBuilder.build(annotation);
    return Response.created(locationOf(annotation)).entity(entity).build();
  }

  private URI locationOf(AlexandriaAnnotation annotation) {
    return URI.create(annotation.getId().toString());
  }

  private void printAnno(UUID annoId) {
    LOG.debug("  anno: [{}]", annoId.toString());
  }
}



