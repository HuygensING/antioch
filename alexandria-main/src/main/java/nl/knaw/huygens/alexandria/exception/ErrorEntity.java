package nl.knaw.huygens.alexandria.exception;

import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.endpoint.JsonWrapperObject;

@JsonTypeName("error")
public class ErrorEntity extends JsonWrapperObject {
  private String message;

  public ErrorEntity(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

}
