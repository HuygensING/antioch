package nl.knaw.huygens.alexandria.endpoint.param;

import java.util.UUID;

public class UUIDParam extends AbstractParam<UUID> {
  public UUIDParam(String param) {
    super(param);
  }

  @Override
  protected UUID parse(String param) {
    return UUID.fromString(param);
  }

  @Override
  protected String getErrorMessage(String param, Throwable e) {
    return String.format("Malformed UUID: %s (%s)", param, e.getMessage());
  }
}
