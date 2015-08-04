package nl.knaw.huygens.alexandria.model;

import java.time.Instant;

public interface Accountable extends Identifiable {
  AlexandriaProvenance getProvenance();

  AlexandriaState getState();

  Instant getStateSince();

  default boolean isTentative() {
    return AlexandriaState.TENTATIVE.equals(getState());
  }
}
