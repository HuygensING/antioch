package nl.knaw.huygens.alexandria.endpoint.resource;

import javax.inject.Inject;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceEntityBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceEntityBuilder.class);

  private final AlexandriaConfiguration config;

  @Inject
  public ResourceEntityBuilder(AlexandriaConfiguration config) {
    LOG.trace("ResourceCreationRequestBuilder created: config=[{}]", config);
    this.config = config;
  }

  public ResourceEntity build(AlexandriaResource resource) {
    return ResourceEntity.of(resource).withConfig(config);
  }
}
