package nl.knaw.huygens.alexandria.model;

import java.util.UUID;

public abstract class AbstractAccountable implements Accountable {
  private final UUID id;
  private final AlexandriaProvenance provenance;

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

}
