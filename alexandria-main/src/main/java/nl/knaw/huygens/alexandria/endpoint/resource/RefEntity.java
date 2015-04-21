package nl.knaw.huygens.alexandria.endpoint.resource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY)
@JsonTypeName("ref")
class RefEntity {
  private final String ref;

  RefEntity(String ref) {
    this.ref = ref;
  }

  public String getRef() {
    return ref;
  }
}
