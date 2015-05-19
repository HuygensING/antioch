package nl.knaw.huygens.alexandria.endpoint.resource;

import java.util.UUID;

public class ResourceCreationRequestBuilder {
	public ResourceCreationRequest build(ResourcePrototype protoType) {
		return new ResourceCreationRequest(protoType);
	}

	public ResourceCreationRequest build(UUID parentId, SubResourcePrototype prototype) {
		// TODO: see comments in ResourceCreationRequest
		// return new ResourceCreationRequest(parentId,prototype);
		return null;
	}
}
