package nl.knaw.huygens.alexandria.about;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.about.AboutEndpoint;
import nl.knaw.huygens.alexandria.endpoint.homepage.HomePageEndpoint;
import nl.knaw.huygens.concordion.AlexandriaAcceptanceTest;

@RunWith(ConcordionRunner.class)
public class AboutFixture extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoint() {
    Log.trace("Registering {}", AboutEndpoint.class.getSimpleName());
    register(AboutEndpoint.class);
    register(HomePageEndpoint.class);
  }

}
