package nl.knaw.huygens.alexandria.app;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

public class TestConfiguration implements AlexandriaConfiguration {

  @Override
  public URI getBaseURI() {
    return UriBuilder.fromUri("http://localhost").port(2015).build();
  }

  @Override
  public String getStorageDirectory() {
    return "c:/tmp/alexandria";
  }

}
