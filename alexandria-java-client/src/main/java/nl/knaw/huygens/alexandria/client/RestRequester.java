package nl.knaw.huygens.alexandria.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.Log;

public class RestRequester<T> {
  private int retries = 5;
  private Supplier<Response> responseSupplier;
  Map<Status, Function<Response, RestResult<T>>> statusMappers = new HashMap<>();
  private Function<Response, RestResult<T>> defaultMapper = (response) -> RestResult.failingResult(response);

  public static <T extends Object> RestRequester<T> withResponseSupplier(Supplier<Response> responseSupplier) {
    RestRequester<T> requester = new RestRequester<>();
    requester.responseSupplier = responseSupplier;
    return requester;
  }

  public RestRequester<T> onStatus(Status status, Function<Response, RestResult<T>> mapper) {
    statusMappers.put(status, mapper);
    return this;
  }

  public RestRequester<T> onOtherStatus(Function<Response, RestResult<T>> defaultMapper) {
    this.defaultMapper = defaultMapper;
    return this;
  }

  public RestResult<T> getResult() {
    RestResult<T> result = new RestResult<>();
    int attempt = 0;
    Response response = null;
    while (response == null && attempt < retries) {
      attempt++;
      Log.info("attempt {}", attempt);
      try {
        response = responseSupplier.get();

      } catch (ProcessingException pe) {
        pe.printStackTrace();

      } catch (Exception e) {
        e.printStackTrace();
        result.setFail(true);
        return result;
      }
    }
    if (response == null) {
      result.setFail(true);
      return result;
    }

    Status status = Status.fromStatusCode(response.getStatus());

    if (statusMappers.containsKey(status)) {
      return statusMappers.get(status).apply(response);

    } else {
      return defaultMapper.apply(response);
    }

  }

  public void setRetries(int retries) {
    this.retries = retries;
  }
}
