package nl.knaw.huygens.alexandria.client;

import java.util.Optional;

import javax.ws.rs.core.Response;

public class RestResult<T> {
  private boolean failure = false;
  private T cargo;
  private Response response;
  private Exception exception;
  private String errorMessage;

  public static <T extends Object> RestResult<T> failingResult(Response response) {
    RestResult<T> result = new RestResult<>();
    result.setFail(true);
    result.setResponse(response);
    return result;
  }

  public static <T extends Object> RestResult<T> failingResult(Exception exception) {
    RestResult<T> result = new RestResult<>();
    result.setFail(true);
    result.setException(exception);
    return result;
  }

  public static <T extends Object> RestResult<T> failingResult(String errorMessage) {
    RestResult<T> result = new RestResult<>();
    result.setFail(true);
    result.setErrorMessage(errorMessage);
    return result;
  }

  public void setCargo(T cargo) {
    this.cargo = cargo;
  }

  public T get() {
    return cargo;
  }

  public void setFail(boolean failure) {
    this.failure = failure;
  }

  public boolean hasFailed() {
    return failure;
  }

  private void setResponse(Response response) {
    this.response = response;
  }

  public Optional<Response> getResponse() {
    return Optional.ofNullable(response);
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }

  public Optional<Exception> getException() {
    return Optional.ofNullable(exception);
  }

  private void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Optional<String> getErrorMessage() {
    return Optional.ofNullable(errorMessage);
  }

}
