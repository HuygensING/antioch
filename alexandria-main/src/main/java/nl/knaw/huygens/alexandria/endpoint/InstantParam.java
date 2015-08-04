package nl.knaw.huygens.alexandria.endpoint;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import nl.knaw.huygens.Log;

public class InstantParam extends AbstractParam<Instant> {

  public InstantParam(String param) {
    super(param);
  }

  @Override
  protected Instant parse(String param) throws Throwable {
    Log.trace("Parsing: [{}]", param);
//    return Instant.parse(param);
    return DateTimeFormatter.ISO_DATE_TIME.parse(param, Instant::from);
  }
}
