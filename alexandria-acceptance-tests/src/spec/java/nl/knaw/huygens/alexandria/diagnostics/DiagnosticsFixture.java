package nl.knaw.huygens.alexandria.diagnostics;

import static nl.knaw.huygens.alexandria.model.AlexandriaState.CONFIRMED;

import java.time.Instant;
import java.util.UUID;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.jersey.exceptionmappers.NotFoundExceptionMapper;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

@RunWith(ConcordionRunner.class)
public class DiagnosticsFixture extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoints() {
    Log.trace("Registering endpoints");
    register(ResourcesEndpoint.class);
    register(NotFoundExceptionMapper.class);
  }

  public void resourceExists(String id) {
    service().createOrUpdateResource(UUID.fromString(id), aRef(), aProvenance(), CONFIRMED);
  }

  private String aRef() {
    return "http://www.example.com/some/ref";
  }

  private TentativeAlexandriaProvenance aProvenance() {
    return new TentativeAlexandriaProvenance("who", Instant.now(), "why");
  }

}
