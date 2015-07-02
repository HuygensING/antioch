package nl.knaw.huygens.alexandria.endpoint;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.resource.PropertyPrefix;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;

public abstract class AbstractAnnotatableEntity extends JsonWrapperObject implements Entity {

  @JsonIgnore
  protected LocationBuilder locationBuilder;

  abstract protected AbstractAnnotatable getAnnotatable();

  @JsonProperty(PropertyPrefix.LINK + "annotations")
  public Set<URI> getAnnotations() {
    Log.debug("Converting {} annotations: [{}]", getAnnotatable().getAnnotations().size(), getAnnotatable().getAnnotations());
    // TODO: When Jackson can handle Streams, maybe return Stream<AnnotationView>.
    final Set<URI> uris = Sets.newHashSet(getAnnotatable().getAnnotations().stream().map(this::annotationURI).iterator());
    Log.debug("uris: {}", uris);
    return uris;
  }

  private URI annotationURI(AlexandriaAnnotation annotation) {
    Log.debug("annotationURI for: [{}], id=[{}]", annotation, annotation.getId());
    return locationBuilder.locationOf(annotation);
  }

  @JsonProperty(PropertyPrefix.LINK + "provenance")
  public URI getProvenance() {
    return URI.create(locationBuilder.locationOf(getAnnotatable()) + "/provenance");
  }

  public UUID getId() {
    return getAnnotatable().getId();
  }

  public Map<String, Object> getState() {
    return ImmutableMap.of(//
        "value", getAnnotatable().getState(), //
        "since", getAnnotatable().getStateSince().toString()//
    );
  }
}
