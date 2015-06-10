package nl.knaw.huygens.alexandria.endpoint.provenance;

import javax.inject.Inject;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;

public class ProvenanceEntityBuilder {
  private final AlexandriaConfiguration config;
  private final EndpointPathResolver resolver;

  @Inject
  public ProvenanceEntityBuilder(AlexandriaConfiguration config, EndpointPathResolver resolver) {
    Log.trace("ProvenanceEntityBuilder created: config=[{}], resolver=[{}]", config, resolver);
    this.resolver = resolver;
    this.config = config;
  }

  public ProvenanceEntity build(AlexandriaProvenance provenance) {
    return ProvenanceEntity.of(provenance).withConfig(config).withResolver(resolver);
  }

}
