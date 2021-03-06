package nl.knaw.huygens.antioch.client;

/*
 * #%L
 * antioch-java-client
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.net.URI;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.SslConfigurator;
import org.junit.Ignore;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.antioch.api.model.AboutEntity;

public class InitializationTest {
  private static final String INSTANCE_HTTPS = "https://antioch.example.org";
  private static final String INSTANCE_HTTP = "http://antioch.example.org";

  @Test
  public void testHttpConnectionWorks() {
    try (AntiochClient client = new AntiochClient(URI.create(INSTANCE_HTTP))) {
      client.setAutoConfirm(true);
      RestResult<AboutEntity> aboutResult = client.getAbout();
      assertThat(aboutResult.hasFailed()).isTrue();
      Log.info("error={}", aboutResult.getErrorMessage());
    }
  }

  @Test
  public void testHttpsConnectionNeedsSSLContext() {
    try {
      AntiochClient client = new AntiochClient(URI.create(INSTANCE_HTTPS));
      fail("RuntimeException expected");
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).isEqualTo("SSL connections need an SSLContext, use: new AntiochClient(uri, sslContext) instead.");
    }
  }

  @Ignore
  @Test
  public void testHttpsConnectionWorks() throws Exception {
    // System.setProperty("javax.net.debug", "ssl");
    SSLContext sslContext = SslConfigurator.newInstance()//
        .keyStoreFile("../keystore.jks")//
        .keyPassword("secret")//
        .trustStoreFile("../truststore.jks")//
        .trustStorePassword("secret")//
        .createSSLContext();
    AntiochClient client = new AntiochClient(URI.create("https://acc.alexandria.huygens.knaw.nl"), sslContext);
    client.setAutoConfirm(true);
    RestResult<AboutEntity> aboutResult = client.getAbout();
    Log.info("result={}", aboutResult);
    client.close();
    assertThat(aboutResult.hasFailed()).isFalse();
    AboutEntity aboutEntity = aboutResult.get();
    Log.info("about={}", aboutEntity);
  }

}
