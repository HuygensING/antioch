package nl.knaw.huygens.alexandria.endpoint.annotation;

import io.swagger.annotations.ApiModel;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import nl.knaw.huygens.alexandria.endpoint.JsonWrapperObject;
import nl.knaw.huygens.alexandria.endpoint.Prototype;
import nl.knaw.huygens.alexandria.endpoint.ProvenancePrototype;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("annotation")
@ApiModel("annotation")
public class AnnotationPrototype extends JsonWrapperObject implements Prototype {
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
