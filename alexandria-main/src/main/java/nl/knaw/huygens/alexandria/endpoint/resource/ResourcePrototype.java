package nl.knaw.huygens.alexandria.endpoint.resource;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import nl.knaw.huygens.alexandria.endpoint.InstantParam;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName("resource")
public class ResourcePrototype {
  private UUIDParam id;

  @NotNull(message = "{nl.knaw.huygens.alexandria.endpoint.resource.ResourceProtoType.ref.NotNull.message}")
  private String ref;

  private InstantParam createdOn;

//  private Set<UUIDParam> annotations;

  public UUIDParam getId() {
    return id;
  }

  public String getRef() {
    return ref;
  }

  public Optional<InstantParam> getCreatedOn() {
    return Optional.ofNullable(createdOn);
  }

//  public Optional<Set<UUIDParam>> getAnnotations() {
//    return Optional.ofNullable(annotations);
//  }
}
