package nl.knaw.huygens.alexandria.endpoint.annotation;

import static java.util.UUID.randomUUID;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.InstantParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AnnotationCreationRequest {
	private static final String DEFAULT_WHY = "why";
	private static final String DEFAULT_WHO = "nederlab";

	private static final Logger LOG = LoggerFactory.getLogger(AnnotationCreationRequest.class);

	private final AnnotationPrototype prototype;

	public AnnotationCreationRequest(AnnotationPrototype prototype) {
		this.prototype = prototype;
	}

	public AlexandriaAnnotation execute(AlexandriaService service) {
		final UUID uuid = randomUUID();

		final TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(DEFAULT_WHO, providedCreatedOn().orElse(Instant.now()), DEFAULT_WHY);

		AlexandriaAnnotationBody body = null;
		if (prototype.getAnnotationBodyId().isPresent()) {
			UUID bodyId = prototype.getAnnotationBodyId().get().getValue();
			body = service.readAnnotationBody(bodyId);

		} else if (prototype.getValue().isPresent()) {
			String value = prototype.getValue().get();
			Optional<String> type = prototype.getType();
			body = service.findAnnotationBodyWithTypeAndValue(type, value);
			if (body == null) {
				service.createAnnotationBody(uuid, type, value, provenance);
			}
		}

		AlexandriaAnnotation annotation = new AlexandriaAnnotation(uuid, body, provenance);

		// // TODO: rewire via the service layer
		// streamAnnotations().map(service::readAnnotation).forEach(
		// annotation::addAnnotation);

		return annotation;
	}

	public boolean wasExecutedAsIs() {
		boolean protoTypeProvidedUUID = prototype.getAnnotationBodyId().isPresent();
		boolean protoTypeProvidedCreatedOn = prototype.getCreatedOn().isPresent();
		final boolean wasExecutedAsIs = protoTypeProvidedUUID && protoTypeProvidedCreatedOn;
		LOG.trace("wasExecutedAsIs: {}", wasExecutedAsIs);
		return wasExecutedAsIs;
	}

	private Optional<String> optionalType() {
		return prototype.getType();
	}

	private String providedValue() {
		return prototype.getValue().get();
	}

	// private Stream<UUID> streamAnnotations() {
	// return
	// prototype.getAnnotations().map(Collection::stream).orElse(Stream.empty()).map(UUIDParam::getValue);
	// }

	private Optional<Instant> providedCreatedOn() {
		return prototype.getCreatedOn().map(InstantParam::getValue);
	}
}
