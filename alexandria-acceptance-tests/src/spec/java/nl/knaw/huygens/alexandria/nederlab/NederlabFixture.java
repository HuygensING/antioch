package nl.knaw.huygens.alexandria.nederlab;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationsEndpoint;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class NederlabFixture extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoints() {
    register(ResourcesEndpoint.class);
    register(AnnotationsEndpoint.class);
  }

  public void resourceExists(String id) {
    UUID uuid = UUID.fromString(id);
    service().createOrUpdateResource(uuid, aRef(), aProvenance(), confirmed());
  }

  public AlexandriaResource subResourceExists(String id, String parentId) {
    UUID uuid = UUID.fromString(id);
    UUID parentUUID = UUID.fromString(parentId);
    final AlexandriaResource subResource = service().createSubResource(uuid, parentUUID, aSub(), aProvenance(), confirmed());
    Log.trace("subResource created: {}", subResource);
    return subResource;
  }

  public void resourceHasAnnotation(String id) {
    UUID uuid = UUID.fromString(id);
    AlexandriaResource resource = service().readResource(uuid).get();
    AlexandriaAnnotationBody annotationBody = anAnnotation();
    service().confirmAnnotation(service().annotate(resource, annotationBody, aProvenance()).getId());
  }

  public void subResourceHasAnnotation(String id, String parentId) {
    AlexandriaResource resource = subResourceExists(id, parentId);
    AlexandriaAnnotationBody annotationBody = anAnnotation();
    service().confirmAnnotation(service().annotate(resource, annotationBody, aProvenance()).getId());
  }
  private AlexandriaAnnotationBody anAnnotation() {
    return service().createAnnotationBody(UUID.randomUUID(), "type", "value", aProvenance(), confirmed());
  }

  private final AtomicInteger id = new AtomicInteger();

  private String aSub() {
    return "/some/folia/expression/" + id.getAndIncrement();
  }

  private TentativeAlexandriaProvenance aProvenance() {
    return new TentativeAlexandriaProvenance("who", Instant.now(), "why");
  }

  private String aRef() {
    return "http://www.example.com/some/ref";
  }

  private AlexandriaState confirmed() {
    return AlexandriaState.CONFIRMED;
  }
}
