package nl.knaw.huygens.alexandria.resource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

import org.concordion.api.ExpectedToFail;

@ExpectedToFail
public class CreationFixture extends ResourceFixture {
	@Override
	public void request(String method, String path) {
		when(resourceService().readResource(any(UUID.class))).thenThrow(new NotFoundException());

		TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("", Instant.now(), "");
		// final AlexandriaResource mockResource = new
		// AlexandriaResource(UUID.randomUUID(), provenance);
		when(resourceService().createOrUpdateResource(any(UUID.class), any(String.class), provenance)).thenReturn(true);

		super.request(method, path);
	}

}
