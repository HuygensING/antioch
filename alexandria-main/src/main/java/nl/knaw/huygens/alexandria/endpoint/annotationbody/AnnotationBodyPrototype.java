package nl.knaw.huygens.alexandria.endpoint.annotationbody;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.endpoint.JsonWrapperObject;
import nl.knaw.huygens.alexandria.endpoint.Prototype;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaState;

@JsonTypeName("annotationBody")
public class AnnotationBodyPrototype extends JsonWrapperObject implements Prototype {
  @NotNull
  private UUIDParam id;

  private String type;

  @NotNull
  private String value;

  private AlexandriaState state;

  public UUIDParam getId() {
    return id;
  }

  public Optional<String> getType() {
    return Optional.ofNullable(type);
  }

  public String getValue() {
    return value;
  }

  public AlexandriaState getState() {
    return state;
  }

}
