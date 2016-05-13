package nl.knaw.huygens.alexandria.client;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

import nl.knaw.huygens.alexandria.api.model.AboutEntity;
import nl.knaw.huygens.alexandria.concordion.AlexandriaAcceptanceTest;
import nl.knaw.huygens.alexandria.endpoint.about.AboutEndpoint;

@RunWith(ConcordionRunner.class)
public class ClientFixture extends AlexandriaAcceptanceTest {

  public String alexandriaVersion() {
    AboutEndpoint aboutEndpoint = new AboutEndpoint(testConfiguration(), service());
    return ((AboutEntity) aboutEndpoint.getMetadata().getEntity()).getVersion();
  }

}
