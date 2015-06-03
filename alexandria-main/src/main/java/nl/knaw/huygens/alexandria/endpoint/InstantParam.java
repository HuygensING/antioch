package nl.knaw.huygens.alexandria.endpoint;

import java.time.Instant;

import nl.knaw.huygens.Log;

public class InstantParam extends AbstractParam<Instant> {

  public InstantParam(String param) {
    super(param);
  }

  @Override
  protected Instant parse(String param) throws Throwable {
    Log.trace("Parsing: [{}]", param);
    return Instant.parse(param);
  }
}
