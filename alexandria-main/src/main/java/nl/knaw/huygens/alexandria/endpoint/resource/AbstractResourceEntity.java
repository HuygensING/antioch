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
    return hasText() ? URI.create(locationBuilder.locationOf(getResource()) + "/text") : null;
  }

  @JsonProperty(PropertyPrefix.LINK + "baseLayerDefinition")
  public URI getBaseLayerDefinition() {
    if (getResource().getDirectBaseLayerDefinition().isPresent()) {
      URI resourceURI = locationBuilder.locationOf(getResource());
      return baseLayerDefinitionURI(resourceURI);
    }

    Optional<IdentifiablePointer<AlexandriaResource>> firstAncestorResourceWithBaseLayerDefinitionPointer = getResource().getFirstAncestorResourceWithBaseLayerDefinitionPointer();
    if (firstAncestorResourceWithBaseLayerDefinitionPointer.isPresent()) {
      URI ancestorURI = locationBuilder.locationOf(firstAncestorResourceWithBaseLayerDefinitionPointer.get());
      return baseLayerDefinitionURI(ancestorURI);
    }
    return null;
  }

  private URI baseLayerDefinitionURI(URI resourceURI) {
    return URI.create(resourceURI + "/" + ResourcesEndpoint.BASELAYERDEFINITION);
  }

  @JsonProperty(PropertyPrefix.LINK + "subresources")
  public Set<URI> getSubResources() {
    return getResource().getSubResourcePointers().stream().map(locationBuilder::locationOf).collect(toSet());
  }

  private AlexandriaResource getResource() {
    return (AlexandriaResource) getAnnotatable();
  }

}