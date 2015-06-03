package nl.knaw.huygens.alexandria.endpoint.resource;

import javax.inject.Inject;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public class ResourceEntityBuilder {

  private final AlexandriaConfiguration config;

  @Inject
  public ResourceEntityBuilder(AlexandriaConfiguration config) {
    Log.trace("ResourceCreationRequestBuilder created: config=[{}]", config);
    this.config = config;
  }

  public ResourceEntity build(AlexandriaResource resource) {
    return ResourceEntity.of(resource).withConfig(config);
  }
}
