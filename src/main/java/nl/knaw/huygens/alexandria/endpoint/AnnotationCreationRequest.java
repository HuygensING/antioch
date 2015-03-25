package nl.knaw.huygens.alexandria.endpoint;

import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import nl.knaw.huygens.alexandria.endpoint.param.InstantParam;
import nl.knaw.huygens.alexandria.endpoint.param.UUIDParam;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("annotation")
class AnnotationCreationRequest {
  UUIDParam id;
  String type;
  String value;
  InstantParam createdOn;
  private Set<UUIDParam> annotations;

  @JsonIgnore
  public Optional<Set<UUIDParam>> getAnnotations() {
    return Optional.ofNullable(annotations);
  }
}
