package nl.knaw.huygens.alexandria.endpoint;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.exception.BadRequestException;

public abstract class AbstractParam<V> {
  private final V value;
  private final String originalParam;

  public AbstractParam(String param) throws BadRequestException {
    this.originalParam = param;
    try {
      this.value = parse(param);
    } catch (Throwable e) {
      final String errorMessage = getErrorMessage(param, e);
      Log.warn("Failed to parse: [{}]: {}", param, errorMessage);
      throw new BadRequestException(errorMessage);
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

  // TODO: wrap error message in a proper error object, so it can be converted to JSON
  protected String getErrorMessage(String param, Throwable e) {
    return String.format("Invalid parameter: %s (%s)", param, e.getMessage());
  }
}