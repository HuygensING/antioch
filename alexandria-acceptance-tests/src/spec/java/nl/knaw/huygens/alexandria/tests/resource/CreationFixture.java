package nl.knaw.huygens.alexandria.tests.resource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import org.concordion.api.ExpectedToFail;

@ExpectedToFail
public class CreationFixture extends ResourceFixture {
  @Override
  public void request(String method, String path) {
    when(storage().exists(any(), any(UUID.class))).thenReturn(false);
    when(storage().readResource(any(UUID.class))).thenReturn(Optional.of(aResource()));

    super.request(method, path);
  }

  private AlexandriaResource aResource() {
    return new AlexandriaResource(UUID.randomUUID(), aProvenance());
  }

  private TentativeAlexandriaProvenance aProvenance() {
    return new TentativeAlexandriaProvenance("nederlab", Instant.now(), "a reason");
  }

}
