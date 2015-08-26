package nl.knaw.huygens.alexandria;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

public class AcceptanceConfiguration implements AlexandriaConfiguration {

  @Override
  public URI getBaseURI() {
    // TODO: hide url
    return UriBuilder.fromUri("https://alexandria.huygens.knaw.nl/").build();
  }

  @Override
  public String getStorageDirectory() {
    return "/data/alexandria/storage/acceptance/";
  }

}
