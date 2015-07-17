package nl.knaw.huygens.alexandria.resource;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import org.junit.BeforeClass;

public class ResourcesBase extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoint() {
    Log.trace("Registering ResourcesEndpoint");
    register(ResourcesEndpoint.class);
  }
}
