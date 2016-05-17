package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import java.net.URI;

import javax.net.ssl.SSLContext;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;

public class InitializationTest {
  private static final String INSTANCE_HTTPS = "https://alexandria.example.org";
  private static final String INSTANCE_HTTP = "http://alexandria.example.org";

  @Test
  public void testHttpConnectionWorks() {
    try (AlexandriaClient client = new AlexandriaClient(URI.create(INSTANCE_HTTP))) {
      client.setAutoConfirm(true);
      RestResult<AboutEntity> aboutResult = client.getAbout();
      assertThat(aboutResult.hasFailed()).isTrue();
      Log.info("error={}", aboutResult.getErrorMessage());
    }
  }

  @Test
  public void testHttpsConnectionNeedsSSLContext() {
    try {
      AlexandriaClient client = new AlexandriaClient(URI.create(INSTANCE_HTTPS));
      fail("RuntimeException expected");
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).isEqualTo("SSL connections need an SSLContext, use: new AlexandriaClient(uri, sslContext) instead.");
    }
  }

  // @Ignore
  @Test
  public void testHttpsConnectionWorks() {
    SSLContext sslContext = mock(SSLContext.class);
    AlexandriaClient client = new AlexandriaClient(URI.create(INSTANCE_HTTPS), sslContext);
    client.setAutoConfirm(true);
    RestResult<AboutEntity> aboutResult = client.getAbout();
    Log.info("result={}", aboutResult);
    client.close();
    // assertThat(aboutResult.hasFailed()).isFalse();
    // AboutEntity aboutEntity = aboutResult.get();
    // Log.info("about={}", aboutEntity);
  }

}
