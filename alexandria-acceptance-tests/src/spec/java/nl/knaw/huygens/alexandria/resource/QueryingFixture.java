package nl.knaw.huygens.alexandria.resource;

import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

public class QueryingFixture extends ResourceFixture {
	private AlexandriaResource resource;

	public void existingResource(String id) {
		UUID uuid = UUID.fromString(id);
		TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
		resource = new AlexandriaResource(uuid, provenance);
		when(service().readResource(uuid)).thenReturn(resource);
	}

	public void withReference(String reference) {
		resource.setRef(reference);
	}

	public void withAnnotation(String id) {
		TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.now(), "why");
		AlexandriaAnnotationBody body = new AlexandriaAnnotationBody(UUID.fromString(id), "<type>", "<value>", provenance);
		resource.addAnnotation(new AlexandriaAnnotation(UUID.fromString(id), body, provenance));
	}

	public void noSuchResource(String id) {
		UUID uuid = UUID.fromString(id);
		when(service().readResource(uuid)).thenThrow(new NotFoundException());
	}
}
