package nl.knaw.huygens.alexandria.about;

import nl.knaw.huygens.alexandria.endpoint.about.AboutEndpoint;
import nl.knaw.huygens.alexandria.endpoint.homepage.HomePageEndpoint;
import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class AboutFixture extends AlexandriaAcceptanceTest {
  @BeforeClass
  public static void registerEndpoint() {
    register(AboutEndpoint.class);
    register(HomePageEndpoint.class);
  }

}
