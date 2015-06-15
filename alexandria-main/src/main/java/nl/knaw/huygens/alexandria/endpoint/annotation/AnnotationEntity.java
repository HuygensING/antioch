package nl.knaw.huygens.alexandria.endpoint.annotation;

import java.util.UUID;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.AnnotatableObjectEntity;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("annotation")
public class AnnotationEntity extends AnnotatableObjectEntity {

  @JsonIgnore
  private final AlexandriaAnnotation annotation;

  @JsonIgnore
  private AlexandriaConfiguration config;

  public static AnnotationEntity of(AlexandriaAnnotation someAnnotation) {
    return new AnnotationEntity(someAnnotation);
  }

  private AnnotationEntity(AlexandriaAnnotation annotation) {
    this.annotation = annotation;
  }

  public final AnnotationEntity withConfig(AlexandriaConfiguration config) {
    this.config = config;
    return this;
  }

  public UUID getId() {
    return annotation.getId();
  }

  @Override
  protected AbstractAnnotatable getAnnotatable() {
    return annotation;
  }

}
