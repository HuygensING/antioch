package nl.knaw.huygens.alexandria.endpoint.annotation;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;

import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AnnotationService;

@Path(EndpointPaths.ANNOTATIONS)
public class AnnotationsEndpoint extends JSONEndpoint {
  private final AnnotationService service;
  private final AnnotationEntityBuilder entityBuilder;
  private final AnnotationCreationCommandBuilder commandBuilder;

  public AnnotationsEndpoint(@Context AnnotationService service, //
                             @Context AnnotationCreationCommandBuilder commandBuilder, //
                             @Context AnnotationEntityBuilder entityBuilder) {
    this.service = service;
    this.commandBuilder = commandBuilder;
    this.entityBuilder = entityBuilder;
  }

  @GET
  @Path("{uuid}")
  public Response readAnnotation(@PathParam("uuid") UUIDParam uuidParam) {
    final AlexandriaAnnotation annotation = service.readAnnotation(uuidParam.getValue());
    final AnnotationEntity entity = entityBuilder.build(annotation);
    return Response.ok(entity).build();
  }

  @POST
  public Response createAnnotation(final AnnotationCreationRequest creationRequest) {
    final AnnotationCreationCommand command = commandBuilder.build(creationRequest);
    final AlexandriaAnnotation annotation = command.execute(service);

    if (command.requiredIntervention()) {
      final AnnotationEntity entity = entityBuilder.build(annotation);
      return Response.created(locationOf(annotation)).entity(entity).build();
    }

    return Response.noContent().build();
  }

  private URI locationOf(AlexandriaAnnotation annotation) {
    return URI.create(annotation.getId().toString());
  }

}



