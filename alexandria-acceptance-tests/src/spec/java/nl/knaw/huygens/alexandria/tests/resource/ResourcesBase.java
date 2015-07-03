package nl.knaw.huygens.alexandria.tests.resource;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.concordion.RestFixture;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import org.junit.BeforeClass;

public class ResourcesBase extends RestFixture {
  @BeforeClass
  public static void registerEndpoint() {
    Log.trace("Registering ResourcesEndpoint");
    register(ResourcesEndpoint.class);
  }
}
