package nl.knaw.huygens.alexandria.endpoint.resource;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public class ResourceEntityBuilder {
  private final AlexandriaConfiguration config;

  public static ResourceEntityBuilder forConfig(AlexandriaConfiguration config) {
    return new ResourceEntityBuilder(config);
  }

  private ResourceEntityBuilder(AlexandriaConfiguration config) {
    this.config = config;
  }

  public ResourceEntity build(AlexandriaResource resource) {
    return ResourceEntity.of(resource).withConfig(config);
  }
}
