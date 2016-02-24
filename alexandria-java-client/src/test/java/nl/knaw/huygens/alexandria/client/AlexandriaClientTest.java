package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.AboutEntity;

public class AlexandriaClientTest {
  URI testURI = URI.create("http://test.alexandria.huygens.knaw.nl/");
  AlexandriaClient client = new AlexandriaClient(testURI);

  @Test
  public void testGetAboutReturnsValidAboutEntity() {
    AboutEntity about = client.getAbout();
    Log.info("about={}", about);
    assertThat(about).isNotNull();

  }
}
