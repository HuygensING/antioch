package nl.knaw.huygens.alexandria.endpoint.annotation;

import java.util.Optional;

import nl.knaw.huygens.alexandria.endpoint.InstantParam;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("annotation")
public class AnnotationPrototype {
	private String type;
	private String value;
	private InstantParam createdOn;
	private UUIDParam annotationBodyId;

	public Optional<UUIDParam> getAnnotationBodyId() {
		return Optional.ofNullable(annotationBodyId);
	}

	public Optional<String> getType() {
		return Optional.ofNullable(type);
	}

	public Optional<String> getValue() {
		return Optional.ofNullable(value);
	}

	public Optional<InstantParam> getCreatedOn() {
		return Optional.ofNullable(createdOn);
	}

}
