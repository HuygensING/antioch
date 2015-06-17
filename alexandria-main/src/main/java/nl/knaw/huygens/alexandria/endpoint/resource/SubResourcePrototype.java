package nl.knaw.huygens.alexandria.endpoint.resource;

import java.util.Optional;

import nl.knaw.huygens.alexandria.endpoint.ProvenancePrototype;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName("resource")
public class SubResourcePrototype {
  private UUIDParam id;

  private ProvenancePrototype provenance;

  public UUIDParam getId() {
    return id;
  }

  public Optional<ProvenancePrototype> getProvenance() {
    return Optional.ofNullable(provenance);
  }

}
