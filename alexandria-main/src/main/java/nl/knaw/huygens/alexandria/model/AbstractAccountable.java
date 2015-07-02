package nl.knaw.huygens.alexandria.model;

import java.time.Instant;
import java.util.UUID;

public abstract class AbstractAccountable implements Accountable {
  private final UUID id;
  private final AlexandriaProvenance provenance;
  private AlexandriaState state = AlexandriaState.TENTATIVE;
  private Instant stateSince = Instant.now();

  protected AbstractAccountable(UUID id, TentativeAlexandriaProvenance provenance) {
    this.id = id;
    this.provenance = provenance.bind(this);
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public AlexandriaProvenance getProvenance() {
    return provenance;
  }

  @Override
  public AlexandriaState getState() {
    return state;
  }

  @Override
  public Instant getStateSince() {
    return stateSince;
  }

  public void setState(AlexandriaState state) {
    this.state = state;
    this.stateSince = Instant.now();
  }

  // Only to be used when deframing from graphdb
  public void setStateSince(Instant instant) {
    this.stateSince = instant;

  }

}
