package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import nl.knaw.huygens.alexandria.endpoint.AbstractAnnotatableEntity;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public abstract class AbstractResourceEntity extends AbstractAnnotatableEntity {

  public AbstractResourceEntity() {
    super();
  }

  public Boolean hasText() {
    return getResource().hasText();
  }

  @JsonProperty(PropertyPrefix.LINK + "text")
  public URI getText() {
    return hasText() ? URI.create(locationBuilder.locationOf(getResource()) + "/text") : null;
  }

  @JsonProperty(PropertyPrefix.LINK + "baseLayer")
  public URI getBaseLayer() {
    return getResource().hasDirectBaseLayer() ? URI.create(locationBuilder.locationOf(getResource()) + "/" + ResourcesEndpoint.BASELAYER) : null;
  }

  @JsonProperty(PropertyPrefix.LINK + "subresources")
  public Set<URI> getSubResources() {
    return getResource().getSubResourcePointers().stream().map(locationBuilder::locationOf).collect(toSet());
  }

  private AlexandriaResource getResource() {
    return (AlexandriaResource) getAnnotatable();
  }

}