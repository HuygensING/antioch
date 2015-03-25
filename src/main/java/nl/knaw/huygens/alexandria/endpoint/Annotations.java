package nl.knaw.huygens.alexandria.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.Instant;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
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
  private final AlexandriaConfiguration config;

  public Annotations(@Context AlexandriaConfiguration config, @Context AnnotationService service) {
    LOG.trace("created: config=[{}], service=[{}]", config, service);
    this.config = config;
    this.service = service;
  }

  @GET
  @Path("/{uuid}")
  public Response readAnnotation(@PathParam("uuid") UUIDParam uuidParam) {
    final AlexandriaAnnotation annotation = service.readAnnotation(uuidParam.getValue());
    return Response.ok(asEntity(annotation)).build();
  }

  @POST
  public Response createAnnotation(AnnotationCreationRequest request) {
    AnnotationRequestValidator.servedBy(service).validate(request);

    final AlexandriaAnnotation annotation = AnnotationCreationHandler.servedBy(service).handle(request);

    return Response.created(locationOf(annotation)).entity(asEntity(annotation)).build();
  }

  private AnnotationView asEntity(AlexandriaAnnotation annotation) {
    return AnnotationView.of(annotation).withConfig(config);
  }

  private URI locationOf(AlexandriaAnnotation annotation) {
    return URI.create(annotation.getId().toString());
  }

  static class AnnotationCreationHandler {
    private final AnnotationService service;

    public AnnotationCreationHandler(AnnotationService service) {
      this.service = service;
    }

    public static AnnotationCreationHandler servedBy(AnnotationService service) {
      return new AnnotationCreationHandler(service);
    }

    public AlexandriaAnnotation handle(AnnotationCreationRequest request) {
      final AlexandriaAnnotation annotation;
      annotation = service.createAnnotation(request.type, request.value);

      LOG.debug("annotation=[{}]", annotation);
      LOG.debug("annotation.annotations=[{}]", annotation.getAnnotations());

      if (request.createdOn == null) {
        LOG.debug("No longer pristine, have to generate createdOn");
        annotation.setCreatedOn(Instant.now());
      } else {
        annotation.setCreatedOn(request.createdOn.getValue());
      }

      return annotation;
    }
  }
}



