package nl.knaw.huygens.alexandria.endpoint.provenance;

import javax.inject.Inject;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;

public class ProvenanceEntityBuilder {
  private final AlexandriaConfiguration config;

  @Inject
  public ProvenanceEntityBuilder(AlexandriaConfiguration config) {
    Log.trace("ProvenanceEntityBuilder created: config=[{}]", config);
    this.config = config;
  }

  public ProvenanceEntity build(AlexandriaProvenance provenance) {
    return ProvenanceEntity.of(provenance).withConfig(config);
  }

}
