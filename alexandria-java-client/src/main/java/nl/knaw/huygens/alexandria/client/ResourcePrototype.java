package nl.knaw.huygens.alexandria.client;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("resource")
public class ResourcePrototype extends AbstractAccountablePrototype<ResourcePrototype> {
  private String ref;

  public ResourcePrototype(String ref) {
    this.setRef(ref);
  }

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

}
