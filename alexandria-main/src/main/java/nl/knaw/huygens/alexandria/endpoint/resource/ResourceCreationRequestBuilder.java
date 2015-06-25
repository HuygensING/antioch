package nl.knaw.huygens.alexandria.endpoint.resource;

import java.util.UUID;

import javax.inject.Singleton;

@Singleton
public class ResourceCreationRequestBuilder {
  public ResourceCreationRequest build(ResourcePrototype protoType) {
    return new ResourceCreationRequest(protoType);
  }

  public SubResourceCreationRequest build(UUID parentId, SubResourcePrototype prototype) {
    return new SubResourceCreationRequest(parentId, prototype);
  }
}
