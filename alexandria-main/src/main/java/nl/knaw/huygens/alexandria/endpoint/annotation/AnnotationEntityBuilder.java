package nl.knaw.huygens.alexandria.endpoint.annotation;

import javax.inject.Inject;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.AnnotationEntity;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationEntityBuilder {
  private final Logger LOG = LoggerFactory.getLogger(AnnotationEntityBuilder.class);

  private final AlexandriaConfiguration config;

  @Inject
  public AnnotationEntityBuilder(AlexandriaConfiguration config) {
    LOG.trace("AnnotationEntityBuilder created: config=[{}]", config);
    this.config = config;
  }

  public AnnotationEntity build(AlexandriaAnnotation annotation) {
    return AnnotationEntity.of(annotation).withConfig(config);
  }

}
