package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;

public class AlexandriaClientTest {
  private static final String AUTHKEY = System.getProperty("ALEXANDRIA_AUTHKEY_TEST", "YHJZHjpke8JYjm5y");
  URI testURI = URI.create("http://test.alexandria.huygens.knaw.nl/");
  AlexandriaClient client = new AlexandriaClient(testURI);

  @Test
  public void testGetAboutReturnsValidAboutEntity() {
    RestResult<AboutEntity> result = client.getAbout();
    assertThat(result.hasFailed()).isFalse();
    AboutEntity about = result.get();
    Log.info("about={}", about);
    assertThat(about).isNotNull();
  }

  @Test
  public void testAddResourceReturnsValidUUID() {
    client.setAuthKey(AUTHKEY);
    ResourcePrototype resource = new ResourcePrototype("corpus");
    RestResult<UUID> result = client.addResource(resource);
    assertThat(result.hasFailed()).isFalse();
    UUID resourceUuid = result.get();
    Log.info("resourceUUID = {}", resourceUuid);
    assertThat(resourceUuid).isNotNull();
  }

  @Test
  public void testAddResourceWithProvenanceReturnsValidUUID() {
    client.setAuthKey(AUTHKEY);
    ResourcePrototype resource = new ResourcePrototype("corpus2").withProvenance(new ProvenancePrototype().setWho("test").setWhy("because test"));
    RestResult<UUID> result = client.addResource(resource);
    assertThat(result.hasFailed()).isFalse();
    UUID resourceUuid = result.get();
    Log.info("resourceUUID = {}", resourceUuid);
    assertThat(resourceUuid).isNotNull();
  }

}
