package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import nl.knaw.huygens.alexandria.endpoint.AbstractAnnotatableEntity;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.IdentifiablePointer;

public abstract class AbstractResourceEntity extends AbstractAnnotatableEntity {

  public AbstractResourceEntity() {
    super();
  }

  public Boolean hasText() {
    return getResource().hasText();
  }

  @JsonProperty(PropertyPrefix.LINK + "text")
  public URI getText() {
    return hasText() ? locationBuilder.locationOf(getResource(), "text") : null;
  }

  @JsonProperty(PropertyPrefix.LINK + "baseLayerDefinition")
  public URI getBaseLayerDefinition() {
    if (getResource().getDirectBaseLayerDefinition().isPresent()) {
      return locationBuilder.locationOf(getResource(), ResourcesEndpoint.BASELAYERDEFINITION);
    }

    Optional<IdentifiablePointer<AlexandriaResource>> firstAncestorResourceWithBaseLayerDefinitionPointer = getResource().getFirstAncestorResourceWithBaseLayerDefinitionPointer();
    if (firstAncestorResourceWithBaseLayerDefinitionPointer.isPresent()) {
      return locationBuilder.locationOf(firstAncestorResourceWithBaseLayerDefinitionPointer.get(), ResourcesEndpoint.BASELAYERDEFINITION);
    }
    return null;
  }

  @JsonProperty(PropertyPrefix.LINK + "subresources")
  public Set<URI> getSubResources() {
    return getResource().getSubResourcePointers().stream().map(locationBuilder::locationOf).collect(toSet());
  }

  private AlexandriaResource getResource() {
    return (AlexandriaResource) getAnnotatable();
  }

}