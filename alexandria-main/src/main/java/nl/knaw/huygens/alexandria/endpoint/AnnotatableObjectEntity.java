package nl.knaw.huygens.alexandria.endpoint;

import java.net.URI;
import java.util.Set;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AnnotatableObject;

import com.google.common.collect.Sets;

public abstract class AnnotatableObjectEntity {

  protected LocationBuilder locationBuilder;

  abstract protected AnnotatableObject getAnnotatable();

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

}
