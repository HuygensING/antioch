package nl.knaw.huygens.alexandria.exception;

public class ErrorEntityBuilder {

  public static ErrorEntity build(String message) {
    return new ErrorEntity(message);
  }

  public static ErrorEntity build(Exception e) {
    return ExceptionErrorEntity.of(e);
  }

}
