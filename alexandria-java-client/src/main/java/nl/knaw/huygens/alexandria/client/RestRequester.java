package nl.knaw.huygens.alexandria.client;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.collect.Maps;

public class RestRequester<T> {
  private Supplier<Response> responseSupplier;
  Map<Status, Function<Response, RestResult<T>>> statusMappers = Maps.newHashMap();

  public static <T extends Object> RestRequester<T> withResponseSupplier(Supplier<Response> responseSupplier) {
    RestRequester<T> requester = new RestRequester<>();
    requester.responseSupplier = responseSupplier;
    return requester;
  }

  public RestRequester<T> onStatus(Status status, Function<Response, RestResult<T>> mapper) {
    statusMappers.put(status, mapper);
    return this;
  }

  public RestResult<T> getResult() {
    RestResult<T> result = new RestResult<>();
    try {
      Response response = responseSupplier.get();
      Status status = Status.fromStatusCode(response.getStatus());

      if (statusMappers.containsKey(status)) {
        return statusMappers.get(status).apply(response);

      } else {
        return RestResult.failingResult(response);
      }

    } catch (Exception e) {
      e.printStackTrace();
      result.setFail(true);
    }

    return result;
  }
}
