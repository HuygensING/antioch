package nl.knaw.huygens.alexandria.tests.resource;

import java.time.Instant;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import org.concordion.api.ExpectedToFail;

@ExpectedToFail
public class CreationFixture extends ResourceFixture {
  @Override
  public void request(String method, String path) {
    Log.trace("request: method=[{}], path=[{}]", method, path);
    super.request(method, path);
  }

  private AlexandriaResource aResource() {
    return new AlexandriaResource(UUID.randomUUID(), aProvenance());
  }

  private TentativeAlexandriaProvenance aProvenance() {
    return new TentativeAlexandriaProvenance("nederlab", Instant.now(), "a reason");
  }

}
