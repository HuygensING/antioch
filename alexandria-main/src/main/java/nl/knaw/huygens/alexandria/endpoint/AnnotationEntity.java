package nl.knaw.huygens.alexandria.endpoint;

import static nl.knaw.huygens.alexandria.endpoint.EndpointPaths.ANNOTATIONS;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Sets;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("annotation")
public class AnnotationEntity {

  @JsonIgnore
  private final AlexandriaAnnotation annotation;

  @JsonIgnore
  private AlexandriaConfiguration config;

  public static AnnotationEntity of(AlexandriaAnnotation someAnnotation) {
    return new AnnotationEntity(someAnnotation);
  }

  private AnnotationEntity(AlexandriaAnnotation annotation) {
    this.annotation = annotation;
  }

  public final AnnotationEntity withConfig(AlexandriaConfiguration config) {
    this.config = config;
    return this;
  }

  public UUID getId() {
    return annotation.getId();
  }

  public Set<URI> getAnnotations() {
    final Set<URI> uris = Sets.newHashSet(annotation.getAnnotations().stream().map(this::annotationURI).iterator());
    Log.debug("uris: {}", uris);
    return uris;
  }

  private URI annotationURI(AlexandriaAnnotation a) {
    Log.debug("annotationURI for: [{}], id=[{}]", a, a.getId());
    return UriBuilder.fromUri(config.getBaseURI()).path(ANNOTATIONS).path("{uuid}").build(a.getId());
  }

}
