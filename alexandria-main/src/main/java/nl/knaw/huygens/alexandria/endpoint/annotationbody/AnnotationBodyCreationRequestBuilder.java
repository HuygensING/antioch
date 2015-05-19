package nl.knaw.huygens.alexandria.endpoint.annotationbody;

public class AnnotationBodyCreationRequestBuilder {

	public AnnotationBodyCreationRequest build(AnnotationBodyPrototype prototype) {
		return new AnnotationBodyCreationRequest(prototype);
	}

}
