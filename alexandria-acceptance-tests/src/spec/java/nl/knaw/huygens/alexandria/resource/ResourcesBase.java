package nl.knaw.huygens.alexandria.resource;

import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import org.junit.BeforeClass;

public class ResourcesBase extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoint() {
    register(ResourcesEndpoint.class);
  }
}
