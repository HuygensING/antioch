package nl.knaw.huygens.alexandria.endpoint.annotationbody;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import nl.knaw.huygens.alexandria.endpoint.UUIDParam;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName("annotationBody")
public class AnnotationBodyPrototype {
	@NotNull
	private UUIDParam id;

	private String type;

	@NotNull
	private String value;

	public UUIDParam getId() {
		return id;
	}

	public Optional<String> getType() {
		return Optional.ofNullable(type);
	}

	public String getValue() {
		return value;
	}

}
