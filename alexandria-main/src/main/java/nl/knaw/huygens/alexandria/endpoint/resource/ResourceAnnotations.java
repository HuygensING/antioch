package nl.knaw.huygens.alexandria.endpoint.resource;

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
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.AnnotationCreationRequest;
import nl.knaw.huygens.alexandria.endpoint.AnnotationCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntity;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationPrototype;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class ResourceAnnotations extends JSONEndpoint {

  private final AlexandriaService service;

  private final UUID uuid;

  private final AnnotationCreationRequestBuilder requestBuilder;

  private final AlexandriaConfiguration configuration;

  private final LocationBuilder locationBuilder;

  @Inject
  public ResourceAnnotations(AlexandriaService service, //
      AnnotationCreationRequestBuilder requestBuilder, //
      AlexandriaConfiguration configuration, //
      LocationBuilder locationBuilder, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    this.locationBuilder = locationBuilder;
    Log.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
    this.service = service;
    this.requestBuilder = requestBuilder;
    this.configuration = configuration;
    this.uuid = uuidParam.getValue();
  }

  @GET
  public Response get() {
    final Set<AlexandriaAnnotation> annotations = service.readResource(uuid).getAnnotations();
    final Set<AnnotationEntity> outgoingAnnos = annotations.stream().map(AnnotationEntity::of).collect(Collectors.toSet());
    return Response.ok(outgoingAnnos).build();
  }

  @POST
  public Response addAnnotation(@NotNull @Valid AnnotationPrototype prototype) {
    AnnotationCreationRequest request = requestBuilder.ofResource(uuid).build(prototype);
    AlexandriaAnnotation annotation = request.execute(service);
    return Response.created(locationBuilder.locationOf(annotation)).build();
  }

}
