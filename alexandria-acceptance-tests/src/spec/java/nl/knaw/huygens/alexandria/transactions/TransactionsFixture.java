package nl.knaw.huygens.alexandria.transactions;

import static java.util.UUID.fromString;
import static nl.knaw.huygens.alexandria.model.AlexandriaState.CONFIRMED;

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

@RunWith(ConcordionRunner.class)
public class TransactionsFixture extends AlexandriaAcceptanceTest {

  @BeforeClass
  public static void registerEndpoints() {
    register(ResourcesEndpoint.class);
    register(AnnotationsEndpoint.class);
    register(SearchEndpoint.class);
  }

  public AlexandriaResource subResourceExists(String id, String parentId) {
    UUID resourceId = fromString(id);
    UUID parentUUID = fromString(parentId);
    return service().createSubResource(resourceId, parentUUID, aSub(), aProvenance(), CONFIRMED);
  }

  public String resourceHasAnnotation(String id) {
    return resourceHasAnnotation(id, anAnnotation("type", "value"));
  }

  public String resourceHasAnnotation(String id, AlexandriaAnnotationBody annotationBody) {
    return hasConfirmedAnnotation(theResource(fromString(id)), annotationBody).toString();
  }

  public String subResourceHasAnnotation(String id, String parentId, String type, String value) {
    return hasConfirmedAnnotation(subResourceExists(id, parentId), anAnnotation(type, value)).toString();
  }

}
