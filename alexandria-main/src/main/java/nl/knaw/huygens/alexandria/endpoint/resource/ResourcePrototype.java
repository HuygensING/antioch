package nl.knaw.huygens.alexandria.endpoint.resource;

import javax.validation.constraints.NotNull;

import nl.knaw.huygens.alexandria.endpoint.AbstractAccountablePrototype;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("resource")
public class ResourcePrototype extends AbstractAccountablePrototype {

  @NotNull(message = "{nl.knaw.huygens.alexandria.endpoint.resource.ResourceProtoType.ref.NotNull.message}")
  private String ref;

  public String getRef() {
    return ref;
  }

}
