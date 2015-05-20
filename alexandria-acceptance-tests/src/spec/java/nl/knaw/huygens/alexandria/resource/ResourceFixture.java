package nl.knaw.huygens.alexandria.resource;

import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.helpers.ApiFixture;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(ConcordionRunner.class)
public class ResourceFixture extends ApiFixture {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceFixture.class);

  @BeforeClass
  public static void registerEndpoint() {
    LOG.trace("Registering ResourcesEndpoint");
    register(ResourcesEndpoint.class);
  }

}
