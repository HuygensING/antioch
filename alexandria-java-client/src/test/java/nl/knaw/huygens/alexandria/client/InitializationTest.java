package nl.knaw.huygens.alexandria.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
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
  public void testHttpsConnectionWorks() throws Exception {
    SSLContext sslContext = sslContext();
    AlexandriaClient client = new AlexandriaClient(URI.create("https://acc.alexandria.huygens.knaw.nl"), sslContext);
    client.setAutoConfirm(true);
    RestResult<AboutEntity> aboutResult = client.getAbout();
    Log.info("result={}", aboutResult);
    client.close();
    // assertThat(aboutResult.hasFailed()).isFalse();
    // AboutEntity aboutEntity = aboutResult.get();
    // Log.info("about={}", aboutEntity);
  }

  private SSLContext sslContext() throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, KeyManagementException {
    // SSLContext sslContext = mock(SSLContext.class);

    InputStream is = new FileInputStream("../cert.pem");
    // You could get a resource as a stream instead.

    X509Certificate caCert = getCertificate(is);

    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(null); // You don't need the KeyStore instance to come from a file.
    keyStore.setCertificateEntry("caCert", caCert);

    tmf.init(keyStore);

    SSLContext sslContext = SSLContext.getInstance("TLSv1");
    sslContext.init(null, tmf.getTrustManagers(), null);
    return sslContext;
  }

  private X509Certificate getCertificate(InputStream is) throws CertificateException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);
    return caCert;
  }

  public SSLContext getSSLContextFromPEM(String pemPath) throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    final X509Certificate cert = getCertificate(pemPath);

    KeyStore keystore = KeyStore.getInstance("JKS");
    keystore.load(null);
    keystore.setCertificateEntry("alias", cert);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(keystore, null);

    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), null, null);
    return sslContext;
  }

  private X509Certificate getCertificate(String pemPath) throws FileNotFoundException, IOException {
    final PEMParser reader = new PEMParser(new FileReader(pemPath));
    // PEMReader reader = new PEMReader(new FileReader(pemPath));
    final X509Certificate cert = (X509Certificate) reader.readObject();
    reader.close();
    return cert;
  }
}
