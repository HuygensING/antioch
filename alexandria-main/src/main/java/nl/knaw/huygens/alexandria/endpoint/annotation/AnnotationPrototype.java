package nl.knaw.huygens.alexandria.endpoint.annotation;

import io.swagger.annotations.ApiModel;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import nl.knaw.huygens.alexandria.endpoint.ProvenancePrototype;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("annotation")
@ApiModel("annotation")
public class AnnotationPrototype {
  private ProvenancePrototype provenance;
  private String type;

  @NotNull
  private String value;

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }

  public String getValue() {
    return value;
  }

  public Optional<ProvenancePrototype> getProvenance() {
    return Optional.ofNullable(provenance);
  }

}
