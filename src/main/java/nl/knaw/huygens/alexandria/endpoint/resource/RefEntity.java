package nl.knaw.huygens.alexandria.endpoint.resource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName("ref")
public class RefEntity {
  public static RefEntity of(String ref) {
    return new RefEntity(ref);
  }

  private final String ref;

  private RefEntity(String ref) {
    this.ref = ref;
  }

  public String getRef() {
    return ref;
  }
}
