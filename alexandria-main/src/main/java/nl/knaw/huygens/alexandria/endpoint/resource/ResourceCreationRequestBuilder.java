package nl.knaw.huygens.alexandria.endpoint.resource;

public class ResourceCreationRequestBuilder {
  public ResourceCreationRequest build(ResourcePrototype protoType) {
    return new ResourceCreationRequest(protoType);
  }
}
