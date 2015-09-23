package nl.knaw.huygens.alexandria.resource;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static nl.knaw.huygens.alexandria.model.AlexandriaState.CONFIRMED;

import java.util.UUID;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;

@RunWith(ConcordionRunner.class)
public class AnatomyFixture extends ResourcesBase {

  public String hasSubresource(String id) {
    return idOf(service().createSubResource(randomUUID(), fromString(id), aSub(), aProvenance(), CONFIRMED));
  }

  public String hasAnnotation(String id) {
    final UUID resId = fromString(id);
    return idOf(service().annotate(theResource(resId), anAnnotationBody(resId), aProvenance()));
  }

  private AlexandriaAnnotationBody anAnnotationBody(UUID resId) {
    return service().createAnnotationBody(resId, aType(), aValue(), aProvenance(), CONFIRMED);
  }

  private String aType() {
    return "type";
  }

  private String aValue() {
    return "value";
  }

}
