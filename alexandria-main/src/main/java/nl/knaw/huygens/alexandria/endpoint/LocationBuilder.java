package nl.knaw.huygens.alexandria.endpoint;

import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.model.Accountable;

public class LocationBuilder {

  private AlexandriaConfiguration config;
  private EndpointPathResolver resolver;

  @Inject
  public LocationBuilder(AlexandriaConfiguration config, EndpointPathResolver resolver) {
    this.config = config;
    this.resolver = resolver;
  }

  public URI locationOf(Accountable accountable) {
    String path = resolver.pathOf(accountable)//
        .orElseThrow(() -> new RuntimeException("unknown Accountable " + accountable.getClass()));
    return UriBuilder.fromUri(config.getBaseURI())//
        .path(path).path("{uuid}").build(accountable.getId());
  }
}
