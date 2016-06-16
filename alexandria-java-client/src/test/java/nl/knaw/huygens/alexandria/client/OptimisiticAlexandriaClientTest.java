package nl.knaw.huygens.alexandria.client;

import org.junit.Test;

import nl.knaw.huygens.alexandria.api.model.AboutEntity;

public class OptimisiticAlexandriaClientTest extends AlexandriaClientTest {

  @Test
  public void testAbout() {
    OptimisticAlexandriaClient c = new OptimisticAlexandriaClient(testURI);
    AboutEntity about = c.getAbout();
    softly.assertThat(about.getVersion()).isNotEmpty();
  }

}
