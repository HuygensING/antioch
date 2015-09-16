package nl.knaw.huygens.alexandria.resource;

import java.time.Instant;
import java.util.UUID;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

@RunWith(ConcordionRunner.class)
public class AnatomyFixture extends ResourcesBase {

  public void resourceExists(String id) {
    Log.trace("resourceExists: id=[{}]", id);
    service().createOrUpdateResource(UUID.fromString(id), aRef(), aProvenance(), confirmed());
  }

  private String aRef() {
    return "http://www.example.com/some/ref";
  }

  private TentativeAlexandriaProvenance aProvenance() {
    return aProvenance("nederlab", Instant.now());
  }

  private TentativeAlexandriaProvenance aProvenance(String who, Instant when) {
    return new TentativeAlexandriaProvenance(who, when, "why");
  }

  private AlexandriaState confirmed() {
    return AlexandriaState.CONFIRMED;
  }
}
