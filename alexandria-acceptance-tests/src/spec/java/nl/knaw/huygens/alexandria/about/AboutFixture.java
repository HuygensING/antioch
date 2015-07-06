package nl.knaw.huygens.alexandria.about;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.AboutEndpoint;
import nl.knaw.huygens.concordion.RestFixture;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class AboutFixture extends RestFixture {
  @BeforeClass
  public static void registerEndpoint() {
    Log.trace("Registering {}", AboutEndpoint.class.getSimpleName());
    register(AboutEndpoint.class);
  }

}
