package nl.knaw.huygens.alexandria.nederlab;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.UUID.fromString;

import java.time.Instant;
import java.util.UUID;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationsEndpoint;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.endpoint.search.SearchEndpoint;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

@RunWith(ConcordionRunner.class)
public class NederlabFixture extends AlexandriaAcceptanceTest {

  @BeforeClass
  public static void registerEndpoints() {
    register(ResourcesEndpoint.class);
    register(AnnotationsEndpoint.class);
    register(SearchEndpoint.class);
  }

  public AlexandriaResource subResourceExists(String id, String parentId) {
    UUID resourceId = fromString(id);
    UUID parentUUID = fromString(parentId);
    return service().createSubResource(resourceId, parentUUID, aSub(), aProvenance(), AlexandriaState.CONFIRMED);
  }

  public String resourceHasAnnotation(String id) {
    return resourceHasAnnotation(id, "type", "value");
  }

  public String resourceHasAnnotation(String resId, String type, String value) {
    return hasConfirmedAnnotation(theResource(fromString(resId)), anAnnotation(type, value)).toString();
  }

  public String resourceExistsWithTagForUserAtInstant(String resId, String value, String who, String when) {
    resourceExists(resId);
    return annotate(theResource(fromString(resId)), aTag(value), aProvenance(who, parse(when)));
  }

  public String subResourceExistsWithAnnotation(String id, String parentId, String type, String value) {
    final AlexandriaResource resource = subResourceExists(id, parentId);
    return hasConfirmedAnnotation(resource, anAnnotation(type, value)).toString();
  }

  private AlexandriaAnnotationBody aTag(String value) {
    return anAnnotation("Tag", value);
  }

  private Instant parse(String when) {
    return ISO_DATE_TIME.parse(when, Instant::from);
  }

  private TentativeAlexandriaProvenance aProvenance(String who, Instant when) {
    return new TentativeAlexandriaProvenance(who, when, "why");
  }

}
