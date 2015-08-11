package nl.knaw.huygens.alexandria.config;

import java.net.URI;

public class MockConfiguration implements AlexandriaConfiguration {

  @Override
  public URI getBaseURI() {
    return URI.create("http://alexandria.eg/");
  }

  @Override
  public String getStorageDirectory() {
    return "/tmp/neo4j-alexandria-mock";
  }

}
