package nl.knaw.huygens.alexandria.endpoint.resource;

import static nl.knaw.huygens.alexandria.endpoint.EndpointPaths.ANNOTATIONS;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Sets;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("resource")
class ResourceEntity {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceEntity.class);

  @JsonIgnore
  private final AlexandriaResource resource;

  @JsonIgnore
  private AlexandriaConfiguration config;

  public static ResourceEntity of(AlexandriaResource someResource) {
    return new ResourceEntity(someResource);
  }

  private ResourceEntity(AlexandriaResource resource) {
    this.resource = resource;
  }

  public final ResourceEntity withConfig(AlexandriaConfiguration config) {
    this.config = config;
    return this;
  }

  public UUID getId() {
    return resource.getId();
  }

  public String getRef() {
    return resource.getRef();
  }

  // TODO: refactor extract common functionality also present in AnnotationEntity.
  public Set<URI> getAnnotations() {
    LOG.debug("Converting {} annotations: [{}]", resource.getAnnotations().size(), resource.getAnnotations());
    // TODO: When Jackson can handle Streams, maybe return Stream<AnnotationView>.
    final Set<URI> uris = Sets.newHashSet(resource.getAnnotations().stream().map(this::annotationURI).iterator());
    LOG.debug("uris: {}", uris);
    return uris;
  }

  private URI annotationURI(AlexandriaAnnotation a) {
    LOG.debug("annotationURI for: [{}], id=[{}]", a, a.getId());
    return UriBuilder.fromUri(config.getBaseURI()).path(ANNOTATIONS).path("{uuid}").build(a.getId());
  }

  public String getCreatedOn() {
    return resource.getProvenance().getWhen().toString(); // ISO-8601 representation
  }

}
