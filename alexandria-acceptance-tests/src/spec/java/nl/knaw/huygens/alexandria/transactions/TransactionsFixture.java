package nl.knaw.huygens.alexandria.transactions;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
public class TransactionsFixture extends AlexandriaAcceptanceTest {
  private final AtomicInteger id = new AtomicInteger();

  @BeforeClass
  public static void registerEndpoints() {
    register(ResourcesEndpoint.class);
    register(AnnotationsEndpoint.class);
    register(SearchEndpoint.class);
  }

  public void resourceExists(String id) {
    service().createOrUpdateResource(UUID.fromString(id), aRef(), aProvenance(), confirmed());
  }

  public AlexandriaResource subResourceExists(String id, String parentId) {
    UUID resourceId = UUID.fromString(id);
    UUID parentUUID = UUID.fromString(parentId);
    return service().createSubResource(resourceId, parentUUID, aSub(), aProvenance(), confirmed());
  }

  public String resourceHasAnnotation(String id) {
    return resourceHasAnnotation(id, "type", "value");
  }

  public String resourceHasAnnotation(String id, String type, String value) {
    final UUID uuid = UUID.fromString(id);
    final AlexandriaResource resource = service().readResource(uuid).get();
    final AlexandriaAnnotationBody annotationBody = anAnnotation(type, value);
    final UUID annotationId = service().annotate(resource, annotationBody, aProvenance()).getId();
    service().confirmAnnotation(annotationId);
    return annotationId.toString();
  }

  public String subResourceHasAnnotation(String id, String parentId, String type, String value) {
    final AlexandriaResource resource = subResourceExists(id, parentId);
    final AlexandriaAnnotationBody annotationBody = anAnnotation(type, value);
    final UUID annotationId = service().annotate(resource, annotationBody, aProvenance()).getId();
    service().confirmAnnotation(annotationId);
    return annotationId.toString();
  }

  private AlexandriaAnnotationBody anAnnotation(String type, String value) {
    return service().createAnnotationBody(UUID.randomUUID(), type, value, aProvenance(), confirmed());
  }

  private String aSub() {
    return "/some/folia/expression/" + id.getAndIncrement();
  }

  private TentativeAlexandriaProvenance aProvenance() {
    return new TentativeAlexandriaProvenance("nederlab", Instant.now(), "why");
  }

  private String aRef() {
    return "http://www.example.com/some/ref";
  }

  private AlexandriaState confirmed() {
    return AlexandriaState.CONFIRMED;
  }
}
