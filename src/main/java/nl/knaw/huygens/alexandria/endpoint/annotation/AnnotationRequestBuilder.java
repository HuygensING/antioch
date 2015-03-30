package nl.knaw.huygens.alexandria.endpoint.annotation;

public interface AnnotationRequestBuilder {
  AnnotationRequest build(AnnotationCreationParameters parameters);
}
