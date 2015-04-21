package nl.knaw.huygens.alexandria.endpoint;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstantParam extends AbstractParam<Instant> {
  private static final Logger LOG = LoggerFactory.getLogger(InstantParam.class);

  public InstantParam(String param) {
    super(param);
  }

  @Override
  protected Instant parse(String param) throws Throwable {
    LOG.trace("Parsing: [{}]", param);
    return Instant.parse(param);
  }
}
