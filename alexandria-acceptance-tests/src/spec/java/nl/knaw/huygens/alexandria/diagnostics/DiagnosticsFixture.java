package nl.knaw.huygens.alexandria.diagnostics;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.jersey.exceptionmappers.NotFoundExceptionMapper;

@RunWith(ConcordionRunner.class)
public class DiagnosticsFixture extends AlexandriaAcceptanceTest {

  @BeforeClass
  public static void registerEndpoints() {
    Log.trace("Registering endpoints");
    register(ResourcesEndpoint.class);
    register(NotFoundExceptionMapper.class);
  }
}
