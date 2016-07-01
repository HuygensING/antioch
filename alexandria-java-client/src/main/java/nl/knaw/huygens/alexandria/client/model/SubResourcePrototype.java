package nl.knaw.huygens.alexandria.client.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;

@JsonTypeName(JsonTypeNames.SUBRESOURCE)
public class SubResourcePrototype extends Prototype {
  ResourcePojo delegate = new ResourcePojo();

  public SubResourcePrototype setSub(String ref) {
    delegate.setRef(ref);
    return this;
  }

  public String getSub() {
    return delegate.getRef();
  }

  public SubResourcePrototype setProvenance(ProvenancePojo provenance) {
    delegate.withProvenance(provenance);
    return this;
  }

  public ProvenancePojo getProvenance() {
    return delegate.getProvenance();
  }

}
