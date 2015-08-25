package nl.knaw.huygens.alexandria.endpoint.annotation;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.alexandria.endpoint.AbstractAccountablePrototype;

@JsonTypeName("annotationUpdate")
@ApiModel("annotationUpdate")
public class AnnotationUpdatePrototype extends AbstractAccountablePrototype {

  @NotNull
  private String value;

  public String getValue() {
    return value;
  }

}
