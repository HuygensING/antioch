package nl.knaw.huygens.alexandria;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

public class TestConfiguration implements AlexandriaConfiguration {

  @Override
  public URI getBaseURI() {
    return UriBuilder.fromUri("http://demo17.huygens.knaw.nl/test-alexandria/").build();
  }

  @Override
  public String getStorageDirectory() {
    return "/data/alexandria/";
  }

}
