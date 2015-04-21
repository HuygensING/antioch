package nl.knaw.huygens.alexandria.endpoint.annotation;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;

public class AnnotationEntityBuilder {
  private final AlexandriaConfiguration config;

  public static AnnotationEntityBuilder forConfig(AlexandriaConfiguration config) {
    return new AnnotationEntityBuilder(config);
  }

  private AnnotationEntityBuilder(AlexandriaConfiguration config) {
    this.config = config;
  }

  public AnnotationEntity build(AlexandriaAnnotation annotation) {
    return AnnotationEntity.of(annotation).withConfig(config);
  }

}
