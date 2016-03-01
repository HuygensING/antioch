package nl.knaw.huygens.alexandria.client;

import javax.ws.rs.core.Response;

public class RestResult<T> {
  private boolean failure = false;
  private T cargo;
  private Response response;

  public static <T extends Object> RestResult<T> failingResult(Response response) {
    RestResult<T> result = new RestResult<>();
    result.setFail(true);
    result.setResponse(response);
    return result;
  }

  public boolean hasFailed() {
    return failure;
  }

  public void setFail(boolean failure) {
    this.failure = failure;
  }

  public T get() {
    return cargo;
  }

  public void setCargo(T cargo) {
    this.cargo = cargo;
  }

  public Response getResponse() {
    return response;
  }

  private void setResponse(Response response) {
    this.response = response;
  }
}
