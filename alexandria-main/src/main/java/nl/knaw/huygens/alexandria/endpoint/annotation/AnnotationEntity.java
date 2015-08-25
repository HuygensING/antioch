package nl.knaw.huygens.alexandria.endpoint.annotation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.alexandria.endpoint.AbstractAnnotatableEntity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.resource.PropertyPrefix;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.IdentifiablePointer;

@JsonTypeName("annotation")
@ApiModel("annotation")
public class AnnotationEntity extends AbstractAnnotatableEntity {

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

  public String getType() {
    return annotation.getBody().getType();
  }

  public String getValue() {
    return annotation.getBody().getValue();
  }

  public Integer getRevision() {
    return annotation.getRevision();
  }

  @JsonProperty(PropertyPrefix.LINK + "annotates")
  public String getAnnotates() {
    IdentifiablePointer<?> annotatablePointer = annotation.getAnnotatablePointer();
    return annotatablePointer == null ? "" : locationBuilder.locationOf(annotatablePointer).toString();
  }

  @JsonProperty(PropertyPrefix.LINK + "deprecates")
  public String getDeprecates() {
    return annotation.getRevision() > 0 ? locationBuilder.locationOf(annotation).toString() + AnnotationsEndpoint.REVPATH + (annotation.getRevision() - 1) : "";
  }

  @JsonProperty(PropertyPrefix.LINK + "versioned_self")
  public String getVersionURL() {
    return locationBuilder.locationOf(annotation).toString() + AnnotationsEndpoint.REVPATH + (annotation.getRevision());
  }

  @JsonProperty(PropertyPrefix.LINK + "current_version")
  public String getCurrentVersionURL() {
    return locationBuilder.locationOf(annotation).toString();
  }

  @Override
  protected AbstractAnnotatable getAnnotatable() {
    return annotation;
  }

}
