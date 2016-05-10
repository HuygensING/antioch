package nl.knaw.huygens.alexandria.client.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("resource")
public class ResourcePrototype extends Prototype {
  ResourcePojo delegate = new ResourcePojo();

  public ResourcePrototype setRef(String ref) {
    delegate.setRef(ref);
    return this;
  }

  public String getRef() {
    return delegate.getRef();
  }

  public ResourcePrototype setProvenance(ProvenancePojo provenance) {
    delegate.withProvenance(provenance);
    return this;
  }

  public ProvenancePojo getProvenance() {
    return delegate.getProvenance();
  }

}
