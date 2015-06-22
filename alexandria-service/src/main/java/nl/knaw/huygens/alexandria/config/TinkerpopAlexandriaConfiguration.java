package nl.knaw.huygens.alexandria.config;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

public class TinkerpopAlexandriaConfiguration implements AlexandriaConfiguration {

  @Override
  public URI getBaseURI() {
    return UriBuilder.fromUri("http://localhost").port(2015).build();
  }

  @Override
  public String getStorageDirectory() {
    return "d:/tmp/neo4j-alexandria";
  }

}
