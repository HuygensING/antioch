package nl.knaw.huygens.alexandria.endpoint.annotation;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.alexandria.endpoint.AbstractAccountablePrototype;

@JsonTypeName("annotation")
@ApiModel("annotation")
public class AnnotationPrototype extends AbstractAccountablePrototype {
  private String type;

  @NotNull
  private String value;

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }

  public String getValue() {
    return value;
  }

}
