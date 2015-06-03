package nl.knaw.huygens.alexandria.endpoint.annotation;

import javax.inject.Inject;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.AnnotationEntity;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;

public class AnnotationEntityBuilder {

  private final AlexandriaConfiguration config;

  @Inject
  public AnnotationEntityBuilder(AlexandriaConfiguration config) {
    Log.trace("AnnotationEntityBuilder created: config=[{}]", config);
    this.config = config;
  }

  public AnnotationEntity build(AlexandriaAnnotation annotation) {
    return AnnotationEntity.of(annotation).withConfig(config);
  }

}
