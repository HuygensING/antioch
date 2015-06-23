package nl.knaw.huygens.alexandria.endpoint.annotation;

import io.swagger.annotations.ApiModel;

import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.AnnotatableObjectEntity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.PropertyPrefix;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("annotation")
@ApiModel("annotation")
public class AnnotationEntity extends AnnotatableObjectEntity {

  @JsonIgnore
  private final AlexandriaAnnotation annotation;

  public static AnnotationEntity of(AlexandriaAnnotation someAnnotation) {
    return new AnnotationEntity(someAnnotation);
  }

  private AnnotationEntity(AlexandriaAnnotation annotation) {
    this.annotation = annotation;
  }

  public final AnnotationEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  public UUID getId() {
    return annotation.getId();
  }

  @JsonProperty(PropertyPrefix.LINK + "annotates")
  public String getAnnotates() {
    return locationBuilder.locationOf(annotation.getAnnotatablePointer()).toString();
  }

  public String getType() {
    return annotation.getBody().getType();
  }

  public String getValue() {
    return annotation.getBody().getValue();
  }

  @Override
  protected AbstractAnnotatable getAnnotatable() {
    return annotation;
  }

}
