package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;

public class AboutTest extends AlexandriaClientTest {
  @Test
  public void testGetAboutReturnsValidAboutEntity() {
    RestResult<AboutEntity> result = client.getAbout();
    assertRequestSucceeded(result);
    AboutEntity about = result.get();
    Log.info("about={}", about);
    assertThat(about).as("about not null").isNotNull();
  }

}
