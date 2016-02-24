package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;

public class AlexandriaClientTest {
  private static final String AUTHKEY = System.getenv("ALEXANDRIA_AUTHKEY_TEST");
  URI testURI = URI.create("http://test.alexandria.huygens.knaw.nl/");
  AlexandriaClient client = new AlexandriaClient(testURI);

  @Test
  public void testGetAboutReturnsValidAboutEntity() {
    Log.info("{}", System.getenv());
    AboutEntity about = client.getAbout();
    Log.info("about={}", about);
    assertThat(about).isNotNull();
  }

  @Test
  public void testAddResourceReturnsValidUUID() {
    client.setAuthKey(AUTHKEY);
    ResourcePrototype resource = new ResourcePrototype("corpus");
    UUID resourceUuid = client.addResource(resource);
    Log.info("resourceUUID = {}", resourceUuid);
    assertThat(resourceUuid).isNotNull();
  }

  @Test
  public void testAddResourceWithProvenanceReturnsValidUUID() {
    client.setAuthKey(AUTHKEY);
    ResourcePrototype resource = new ResourcePrototype("corpus2").withProvenance(new ProvenancePrototype().setWho("test").setWhy("because test"));
    UUID resourceUuid = client.addResource(resource);
    Log.info("resourceUUID = {}", resourceUuid);
    assertThat(resourceUuid).isNotNull();
  }

}
