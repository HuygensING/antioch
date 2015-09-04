package nl.knaw.huygens.alexandria;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

public class TestConfiguration implements AlexandriaConfiguration {

  @Override
  public URI getBaseURI() {
    // TODO: hide url
    return UriBuilder.fromUri("http://test.alexandria.huygens.knaw.nl/").build();
  }

  @Override
  public String getStorageDirectory() {
    return "/data/alexandria/storage/test/";
  }

}
