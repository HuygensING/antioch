package nl.knaw.huygens.alexandria.endpoint.resource;

import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import nl.knaw.huygens.alexandria.endpoint.InstantParam;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName("resource")
public class ResourcePrototype {
  private UUIDParam id;
  private String ref;
  private InstantParam createdOn;
  private Set<UUIDParam> annotations;

  public Optional<UUIDParam> getId() {
    return Optional.ofNullable(id);
  }

  public Optional<String> getRef() {
    return Optional.ofNullable(ref);
  }

  public Optional<InstantParam> getCreatedOn() {
    return Optional.ofNullable(createdOn);
  }

  public Optional<Set<UUIDParam>> getAnnotations() {
    return Optional.ofNullable(annotations);
  }
}
