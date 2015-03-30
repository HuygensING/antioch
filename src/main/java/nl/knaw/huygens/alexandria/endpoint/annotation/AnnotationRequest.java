package nl.knaw.huygens.alexandria.endpoint.annotation;

import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AnnotationService;

interface AnnotationRequest {
  AlexandriaAnnotation execute(AnnotationService service);
}
