package nl.knaw.huygens.alexandria.endpoint.resource;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import nl.knaw.huygens.alexandria.endpoint.ProvenancePrototype;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaState;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName("resource")
public class ResourcePrototype {
  private UUIDParam id;

  @NotNull(message = "{nl.knaw.huygens.alexandria.endpoint.resource.ResourceProtoType.ref.NotNull.message}")
  private String ref;

  private ProvenancePrototype provenance;
  private AlexandriaState state;

  public UUIDParam getId() {
    return id;
  }

  public String getRef() {
    return ref;
  }

  public AlexandriaState getState() {
    return state;
  }

  public Optional<ProvenancePrototype> getProvenance() {
    return Optional.ofNullable(provenance);
  }

  @JsonIgnore
  public void setState(AlexandriaState state) {
    this.state = state;
  }
}
