package nl.knaw.huygens.alexandria.endpoint.annotation;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("annotation")
class AnnotationEntity {
	private static final Logger LOG = LoggerFactory.getLogger(AnnotationEntity.class);

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

	// public String getType() {
	// return annotation.getType();
	// }
	//
	// public String getValue() {
	// return annotation.getValue();
	// }

	// // TODO: refactor extract common functionality also present in
	// // AnnotationEntity.
	// public Set<URI> getAnnotations() {
	// LOG.debug("Converting {} annotations: [{}]",
	// annotation.getAnnotations().size(), annotation.getAnnotations());
	// // TODO: When Jackson can handle Streams, maybe return
	// // Stream<AnnotationView>.
	// final Set<URI> uris =
	// Sets.newHashSet(annotation.streamAnnotations().map(this::annotationURI).iterator());
	// LOG.debug("uris: {}", uris);
	// return uris;
	// }

	private URI annotationURI(AlexandriaAnnotation a) {
		LOG.debug("annotationURI for: [{}], id=[{}]", a, a.getId());
		final String annotationId = a.getId().toString();
		return UriBuilder.fromUri(config.getBaseURI()).path(EndpointPaths.ANNOTATIONS).path(annotationId).build();
	}

	// public String getCreatedOn() {
	// return annotation.getCreatedOn().toString(); // ISO-8601 representation
	// }

}
