package nl.knaw.huygens.alexandria.client.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("annotation")
public class AnnotationPrototype extends Prototype {
  AnnotationPojo delegate = new AnnotationPojo();

  public AnnotationPrototype setType(String type) {
    delegate.setType(type);
    return this;
  }

  public String getType() {
    return delegate.getType();
  }

  public AnnotationPrototype setValue(String value) {
    delegate.setValue(value);
    return this;
  }

  public String getValue() {
    return delegate.getValue();
  }

  public AnnotationPrototype setLocator(String locator) {
    delegate.setLocator(locator);
    return this;
  }

  public String getLocator() {
    return delegate.getLocator();
  }

  public AnnotationPrototype setProvenance(ProvenancePojo provenance) {
    delegate.withProvenance(provenance);
    return this;
  }

  public ProvenancePojo getProvenance() {
    return delegate.getProvenance();
  }

}
