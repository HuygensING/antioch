package nl.knaw.huygens.alexandria.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public abstract class AbstractParam<V> {
  private final V value;
  private final String originalParam;

  public AbstractParam(String param) throws WebApplicationException {
    this.originalParam = param;
    try {
      this.value = parse(param);
    } catch (Throwable e) {
      throw new WebApplicationException(onError(param, e));
    }
  }

  public V getValue() {
    return value;
  }

  public String getOriginalParam() {
    return originalParam;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  protected abstract V parse(String param) throws Throwable;

  protected Response onError(String param, Throwable e) {
    return Response.status(Status.BAD_REQUEST).entity(getErrorMessage(param, e)).build();
  }

  // TODO: wrap error message in a proper error object, so it can be converted to JSON
  protected String getErrorMessage(String param, Throwable e) {
    return String.format("Invalid parameter: %s (%s)", param, e.getMessage());
  }
}