package nl.knaw.huygens.alexandria.endpoint.annotation;

public interface AnnotationCommandBuilder {
  AnnotationCommand build(AnnotationCreationRequest parameters);
}
