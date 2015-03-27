package nl.knaw.huygens.alexandria;

import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import nl.knaw.huygens.alexandria.endpoint.param.InstantParam;
import nl.knaw.huygens.alexandria.endpoint.param.UUIDParam;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("annotation")
public class AnnotationCreationParameters {
  private UUIDParam id;
  private String type;
  private String value;
  private InstantParam createdOn;
  private Set<UUIDParam> annotations;

  public Optional<UUIDParam> getId() {
    return Optional.ofNullable(id);
  }

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }

  public Optional<String> getValue() {
    return Optional.ofNullable(value);
  }

  public Optional<InstantParam> getCreatedOn() {
    return Optional.ofNullable(createdOn);
  }

  public Optional<Set<UUIDParam>> getAnnotations() {
    return Optional.ofNullable(annotations);
  }
}
