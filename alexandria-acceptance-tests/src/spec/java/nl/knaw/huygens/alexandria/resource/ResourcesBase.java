package nl.knaw.huygens.alexandria.resource;

import org.junit.BeforeClass;

import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;

public class ResourcesBase extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoint() {
    register(ResourcesEndpoint.class);
  }
}
