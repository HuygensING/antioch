package nl.knaw.huygens.alexandria.endpoint.annotation;

import javax.inject.Inject;
import javax.ws.rs.PathParam;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.AnnotatableObjectAnnotations;
import nl.knaw.huygens.alexandria.endpoint.AnnotationCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AnnotatableObject;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationAnnotations extends AnnotatableObjectAnnotations {

  // TODO: how to remove this duplicated inject/constructor?
  @Inject
  public AnnotationAnnotations(AlexandriaService service, //
      AnnotationCreationRequestBuilder requestBuilder, //
      LocationBuilder locationBuilder, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    super(service, requestBuilder, locationBuilder, uuidParam);
    Log.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
  }

  @Override
  protected AnnotatableObject getAnnotableObject() {
    return service.readAnnotation(uuid);
  };

  @Override
  protected AnnotationCreationRequestBuilder getAnnotationCreationRequestBuilder() {
    return requestBuilder.ofAnnotation(uuid);
  };

}
