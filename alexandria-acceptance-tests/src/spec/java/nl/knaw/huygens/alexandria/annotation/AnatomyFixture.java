package nl.knaw.huygens.alexandria.annotation;

import static java.util.UUID.fromString;

import java.time.Instant;
import java.util.UUID;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

@RunWith(ConcordionRunner.class)
public class AnatomyFixture extends AnnotationsBase {

  public void resourceExists(String id) {
    service().createOrUpdateResource(fromString(id), aRef(), aProvenance(), AlexandriaState.CONFIRMED);
  }

  public String hasAnnotation(String id) {
    final UUID uuid = fromString(id);
    return annotate(theResource(uuid), anAnnotationBody(uuid), aProvenance());
  }

  private AlexandriaAnnotationBody anAnnotationBody(UUID resId) {
    return service().createAnnotationBody(resId, aType(), aValue(), aProvenance(), AlexandriaState.CONFIRMED);
  }

  private String aType() {
    return "type";
  }

  private String aValue() {
    return "value";
  }

  private TentativeAlexandriaProvenance aProvenance(String who, Instant when) {
    return new TentativeAlexandriaProvenance(who, when, "why");
  }

}
