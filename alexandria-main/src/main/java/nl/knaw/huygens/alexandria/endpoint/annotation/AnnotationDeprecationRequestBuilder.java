package nl.knaw.huygens.alexandria.endpoint.annotation;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.stream.Stream;

import javax.inject.Inject;

import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationDeprecationRequestBuilder {
  private final AlexandriaService service;
  private AlexandriaAnnotation originalAnnotation;

  @Inject
  public AnnotationDeprecationRequestBuilder(AlexandriaService service) {
    this.service = requireNonNull(service, "AlexandriaService MUST not be null");
  }

  public AnnotationDeprecationRequestBuilder ofAnnotation(AlexandriaAnnotation annotation) {
    this.originalAnnotation = annotation;
    return this;
  }

  public AnnotationDeprecationRequest build(AnnotationPrototype prototype) {
    return new AnnotationDeprecationRequest(originalAnnotation, prototype);
  }

  protected <T> Stream<T> stream(Collection<T> c) {
    return c.parallelStream(); // override in case you prefer stream() over parallelStream()
  }

}
