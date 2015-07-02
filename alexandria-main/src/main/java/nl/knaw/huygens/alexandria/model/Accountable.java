package nl.knaw.huygens.alexandria.model;

import java.time.Instant;
import java.util.UUID;

public interface Accountable {
  UUID getId();

  AlexandriaProvenance getProvenance();

  AlexandriaState getState();

  Instant getStateSince();
}
