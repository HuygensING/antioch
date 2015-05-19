package nl.knaw.huygens.alexandria.endpoint.annotation;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import nl.knaw.huygens.alexandria.endpoint.InstantParam;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("annotation")
public class AnnotationPrototype {
	private InstantParam createdOn;
	@NotNull
	@ExistingAnnotationBody
	private UUIDParam annotationBodyId;

	public UUIDParam getAnnotationBodyId() {
		return annotationBodyId;
	}

	public Optional<InstantParam> getCreatedOn() {
		return Optional.ofNullable(createdOn);
	}

}
