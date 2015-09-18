package nl.knaw.huygens.alexandria.resource;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;

import java.time.Instant;
import java.util.UUID;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

@RunWith(ConcordionRunner.class)
public class AnatomyFixture extends ResourcesBase {

  public void resourceExists(String id) {
    service().createOrUpdateResource(fromString(id), aRef(), aProvenance(), confirmed());
  }

  public String hasSubresource(String id) {
    return service().createSubResource(randomUUID(), fromString(id), aSub(), aProvenance(), confirmed()) //
        .getId().toString();
  }

  public String hasAnnotation(String id) {
    final UUID uuid = fromString(id);
    return service().annotate(theResource(uuid), anAnnotationBody(uuid), aProvenance()).getId().toString();
  }

  private AlexandriaResource theResource(UUID resId) {
    return service().readResource(resId).get();
  }

  private AlexandriaAnnotationBody anAnnotationBody(UUID resId) {
    return service().createAnnotationBody(resId, aType(), aValue(), aProvenance(), confirmed());
  }

  private String aSub() {
    return "/some/folia/expression";
  }

  private String aType() {
    return "type";
  }

  private String aValue() {
    return "value";
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
