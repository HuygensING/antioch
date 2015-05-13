package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.InstantParam;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ResourceCreationRequest {
	private static final String DEFAULT_WHY = "why";

	private static final String DEFAULT_WHO = "nederlab";

	private static final Logger LOG = LoggerFactory.getLogger(ResourceCreationRequest.class);

	private final ResourcePrototype prototype;
	private boolean resourceCreated;

	private UUID uuid;

	ResourceCreationRequest(ResourcePrototype prototype) {
		this.prototype = prototype;
	}

	public void execute(AlexandriaService service) {
		LOG.trace("executing, service=[{}]", service);

		uuid = providedUUID().orElse(UUID.randomUUID());

		String ref = providedRef();
		TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(DEFAULT_WHO, providedCreatedOn().orElse(Instant.now()), DEFAULT_WHY);
		resourceCreated = service.createOrUpdateResource(uuid, ref, provenance);

		// LOG.trace("resource: [{}]", resource);
	}

	public boolean wasExecutedAsIs() {
		final boolean wasExecutedAsIs = providedUUID().isPresent() && providedCreatedOn().isPresent();
		LOG.trace("wasExecutedAsIs: {}", wasExecutedAsIs);
		return wasExecutedAsIs;
	}

	public boolean newResourceWasCreated() {
		LOG.trace("newResourceWasCreated: {}", resourceCreated);
		return resourceCreated;
	}

	public UUID getUUID() {
		return uuid;
	}

	private String providedRef() {
		return requireNonNull(prototype.getRef(), "Required 'ref' field was not validated for being non-null");
	}

	private Optional<UUID> providedUUID() {
		return Optional.ofNullable(prototype.getId()).map(UUIDParam::getValue);
	}

	private Optional<Instant> providedCreatedOn() {
		return prototype.getCreatedOn().map(InstantParam::getValue);
	}
}
