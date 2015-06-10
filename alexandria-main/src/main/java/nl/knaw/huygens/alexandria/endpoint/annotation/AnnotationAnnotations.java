package nl.knaw.huygens.alexandria.endpoint.annotation;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.AnnotationCreationRequest;
import nl.knaw.huygens.alexandria.endpoint.AnnotationCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationAnnotations extends JSONEndpoint {

  private final AlexandriaService service;
  private final UUID parentUuid;
  private final AnnotationCreationRequestBuilder requestBuilder;
  private final LocationBuilder locationBuilder;

  @Inject
  public AnnotationAnnotations(AlexandriaService service, //
      AnnotationCreationRequestBuilder requestBuilder, //
      LocationBuilder locationBuilder, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    Log.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
    this.service = service;
    this.requestBuilder = requestBuilder;
    this.parentUuid = uuidParam.getValue();
    this.locationBuilder = locationBuilder;
  }

  @GET
  public Response get() {
    final Set<AlexandriaAnnotation> annotations = service.readResource(parentUuid).getAnnotations();
    final Set<AnnotationEntity> outgoingAnnos = annotations.stream().map(AnnotationEntity::of).collect(Collectors.toSet());
    return Response.ok(outgoingAnnos).build();
  }

  @POST
  public Response addAnnotation(@NotNull @Valid AnnotationPrototype prototype) {
    AnnotationCreationRequest request = requestBuilder.ofAnnotation(parentUuid).build(prototype);
    AlexandriaAnnotation annotation = request.execute(service);
    return Response.created(locationBuilder.locationOf(annotation)).build();
  }
}
