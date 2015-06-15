package nl.knaw.huygens.alexandria.endpoint;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntity;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationPrototype;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public abstract class AnnotatableObjectAnnotationsEndpoint extends JSONEndpoint {

  protected final AlexandriaService service;
  protected final LocationBuilder locationBuilder;
  protected final AnnotationCreationRequestBuilder requestBuilder;
  protected final UUID uuid;

  protected AnnotatableObjectAnnotationsEndpoint(AlexandriaService service, //
      AnnotationCreationRequestBuilder requestBuilder, //
      LocationBuilder locationBuilder, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    Log.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
    this.service = service;
    this.requestBuilder = requestBuilder;
    this.uuid = uuidParam.getValue();
    this.locationBuilder = locationBuilder;
  }

  protected abstract AbstractAnnotatable getAnnotableObject();

  protected abstract AnnotationCreationRequestBuilder getAnnotationCreationRequestBuilder();

  @GET
  public Response get() {
    final Set<AlexandriaAnnotation> annotations = getAnnotableObject().getAnnotations();
    final Set<AnnotationEntity> outgoingAnnos = annotations.stream().map(AnnotationEntity::of).collect(Collectors.toSet());
    return Response.ok(outgoingAnnos).build();
  }

  @POST
  public Response addAnnotation(@NotNull @Valid AnnotationPrototype prototype) {
    AnnotationCreationRequest request = getAnnotationCreationRequestBuilder().build(prototype);
    AlexandriaAnnotation annotation = request.execute(service);
    return Response.created(locationBuilder.locationOf(annotation)).build();
  }

}
