package nl.knaw.huygens.alexandria.tests.resource;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.helpers.RestFixture;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class ResourceFixture extends RestFixture {

  @BeforeClass
  public static void registerEndpoint() {
    Log.trace("Registering ResourcesEndpoint");
    register(ResourcesEndpoint.class);
  }

}
