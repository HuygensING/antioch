package nl.knaw.huygens.alexandria.endpoint;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import nl.knaw.huygens.alexandria.model.AlexandriaState;

public abstract class AbstractAccountablePrototype extends JsonWrapperObject implements Prototype {
  private UUIDParam id;
  private ProvenancePrototype provenance;
  private AlexandriaState state;

  public UUIDParam getId() {
    return id;
  }

  public void setId(UUIDParam uuid) {
    id = uuid;
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
