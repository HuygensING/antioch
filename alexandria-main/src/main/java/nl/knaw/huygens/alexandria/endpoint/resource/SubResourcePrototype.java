package nl.knaw.huygens.alexandria.endpoint.resource;

import nl.knaw.huygens.alexandria.endpoint.AbstractAccountablePrototype;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("subresource")
public class SubResourcePrototype extends AbstractAccountablePrototype {
  private String sub;

  public String getSub() {
    return sub;
  }

}
