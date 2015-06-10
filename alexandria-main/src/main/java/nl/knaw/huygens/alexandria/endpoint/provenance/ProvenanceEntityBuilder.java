package nl.knaw.huygens.alexandria.endpoint.provenance;

import javax.inject.Inject;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;

public class ProvenanceEntityBuilder {
  private final AlexandriaConfiguration config;
  private final LocationBuilder locationBuilder;

  @Inject
  public ProvenanceEntityBuilder(AlexandriaConfiguration config, LocationBuilder locationBuilder) {
    Log.trace("ProvenanceEntityBuilder created: config=[{}], locationBuilder=[{}]", config, locationBuilder);
    this.config = config;
    this.locationBuilder = locationBuilder;
  }

  public ProvenanceEntity build(AlexandriaProvenance provenance) {
    return ProvenanceEntity.of(provenance).withConfig(config).withLocationBuilder(locationBuilder);
  }

}
