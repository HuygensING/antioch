package nl.knaw.huygens.alexandria.config;

import java.net.URI;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

public class MockConfiguration implements AlexandriaConfiguration {

  @Override
  public URI getBaseURI() {
    return URI.create("http://alexandria.org/");
  }

}
