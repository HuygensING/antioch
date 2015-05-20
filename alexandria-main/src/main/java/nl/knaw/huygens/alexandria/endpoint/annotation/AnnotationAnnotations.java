package nl.knaw.huygens.alexandria.endpoint.annotation;

import static nl.knaw.huygens.alexandria.endpoint.EndpointPaths.ANNOTATIONS;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.AnnotationCreationRequest;
import nl.knaw.huygens.alexandria.endpoint.AnnotationCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.AnnotationEntity;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationAnnotations extends JSONEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(AnnotationAnnotations.class);

	private final AlexandriaService service;

	private final UUID uuid;

	private final AnnotationCreationRequestBuilder requestBuilder;

	private final AlexandriaConfiguration configuration;

	@Inject
	public AnnotationAnnotations(AlexandriaService service, //
			AnnotationCreationRequestBuilder requestBuilder, //
			AlexandriaConfiguration configuration, //
			@PathParam("uuid") final UUIDParam uuidParam) {
		LOG.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
		this.service = service;
		this.requestBuilder = requestBuilder;
		this.configuration = configuration;
		this.uuid = uuidParam.getValue();
	}

	@GET
	public Response get() {
		final Set<AlexandriaAnnotation> annotations = service.readResource(uuid).getAnnotations();
		final Set<AnnotationEntity> outgoingAnnos = annotations.stream().map(AnnotationEntity::of).collect(Collectors.toSet());
		return Response.ok(outgoingAnnos).build();
	}

	@POST
	public Response addAnnotation(@NotNull @Valid AnnotationPrototype prototype) {
		AnnotationCreationRequest request = requestBuilder.ofAnnotation(uuid).build(prototype);
		request.execute(service);
		return Response.created(locationOf(uuid)).build();
	}

	// TODO: replace by injected LocationBuilder (to be written) ?
	private URI locationOf(UUID uuid) {
		return UriBuilder.fromUri(configuration.getBaseURI()).path(ANNOTATIONS).path("{uuid}").build(uuid);
	}

}
