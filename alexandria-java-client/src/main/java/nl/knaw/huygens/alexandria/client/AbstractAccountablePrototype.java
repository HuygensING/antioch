package nl.knaw.huygens.alexandria.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;

@JsonInclude(Include.NON_NULL)
public abstract class AbstractAccountablePrototype<T> extends JsonWrapperObject {
  private ProvenancePrototype provenance;

  public T withProvenance(ProvenancePrototype provenance) {
    this.provenance = provenance;
    return (T) this;
  }

  public ProvenancePrototype getProvenance() {
    return provenance;
  }

}
