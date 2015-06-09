package nl.knaw.huygens.alexandria.endpoint.annotationbody;

import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Path(EndpointPaths.ANNOTATIONBODIES)
public class AnnotationBodiesEndpoint extends JSONEndpoint {
	private final AlexandriaService service;
	private final AnnotationBodyEntityBuilder entityBuilder;
	private final AnnotationBodyCreationRequestBuilder requestBuilder;

	@Inject
	public AnnotationBodiesEndpoint(AlexandriaService service, //
			AnnotationBodyCreationRequestBuilder requestBuilder, //
			AnnotationBodyEntityBuilder entityBuilder) {
		this.service = service;
		this.requestBuilder = requestBuilder;
		this.entityBuilder = entityBuilder;
	}

	@GET
	@Path("{uuid}")
	public Response readAnnotationBody(@PathParam("uuid") UUIDParam uuidParam) {
		final AlexandriaAnnotationBody annotationBody = service.readAnnotationBody(uuidParam.getValue());
		final AnnotationBodyEntity entity = entityBuilder.build(annotationBody);
		return Response.ok(entity).build();
	}

	@POST
	public Response createAnnotationBody(final AnnotationBodyPrototype prototype) {
		final AnnotationBodyCreationRequest request = requestBuilder.build(prototype);
		request.execute(service);

		if (request.wasCreated()) {
			return Response.created(locationOf(prototype.getId().getValue())).build();
		}

		return Response.status(Status.CONFLICT).build();
	}

	@DELETE
	@Path("{uuid}")
	public Response deleteNotSupported(@PathParam("uuid") final UUIDParam paramId) {
		return methodNotImplemented();
	}

	private URI locationOf(UUID uuid) {
		return URI.create(String.format("%s/%s", EndpointPaths.ANNOTATIONBODIES + "/", uuid));
	}

}
