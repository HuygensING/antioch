package nl.knaw.huygens.alexandria.endpoint;

import javax.inject.Inject;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;

public class ProvenanceEntityBuilder {
  private final LocationBuilder locationBuilder;

  @Inject
  public ProvenanceEntityBuilder(LocationBuilder locationBuilder) {
    Log.trace("ProvenanceEntityBuilder created: locationBuilder=[{}]", locationBuilder);
    this.locationBuilder = locationBuilder;
  }

  public ProvenanceEntity build(AlexandriaProvenance provenance) {
    return ProvenanceEntity.of(provenance).withLocationBuilder(locationBuilder);
  }

}
