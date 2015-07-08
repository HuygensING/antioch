package nl.knaw.huygens.alexandria;

import java.net.URI;

public class TestConfiguration implements AlexandriaConfiguration {

  @Override
  public URI getBaseURI() {
    // TODO: hide url
    return UriBuilder.fromUri("http://tc23.huygens.knaw.nl/test-alexandria/").build();
  }

  @Override
  public String getStorageDirectory() {
    return "/data/alexandria/storage/";
  }

}
