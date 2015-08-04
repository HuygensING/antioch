package nl.knaw.huygens.alexandria.nederlab;

import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class NederlabFixture extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoint() {
    register(ResourcesEndpoint.class);
  }
}
