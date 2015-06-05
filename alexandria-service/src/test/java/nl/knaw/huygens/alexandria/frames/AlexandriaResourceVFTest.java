package nl.knaw.huygens.alexandria.frames;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.util.UUID;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import org.junit.Test;

public class AlexandriaResourceVFTest {
  @Test
  public void testGetAfterOfIsIdentity() {

    UUID id = UUID.randomUUID();
    String who = "who";
    String why = "why";
    Instant when = Instant.now();
    String ref = "ref";

    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(who, when, why);
    AlexandriaResource r = new AlexandriaResource(id, provenance);
    r.setRef(ref);
    r.setState(AlexandriaState.Expired);

    AlexandriaResourceVF rf = AlexandriaResourceVF.of(r);
    AlexandriaResource r2 = rf.get();

    Log.info("resource in={}", r);
    Log.info("resource out={}", r2);
    assertThat(r2).isEqualTo(r);
  }
}
