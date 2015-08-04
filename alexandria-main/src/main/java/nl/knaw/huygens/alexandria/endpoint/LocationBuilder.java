package nl.knaw.huygens.alexandria.endpoint;

import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.model.Identifiable;
import nl.knaw.huygens.alexandria.model.IdentifiablePointer;

public class LocationBuilder {

  private AlexandriaConfiguration config;
  private EndpointPathResolver resolver;

  @Inject
  public LocationBuilder(AlexandriaConfiguration config, EndpointPathResolver resolver) {
    this.config = config;
    this.resolver = resolver;
  }

  public URI locationOf(Identifiable accountable) {
    String path = resolver.pathOf(accountable)//
        .orElseThrow(() -> new RuntimeException("unknown Identifiable class " + accountable.getClass()));
    return UriBuilder.fromUri(config.getBaseURI())//
        .path(path).path("{uuid}").build(accountable.getId());
  }

  public URI locationOf(Class<? extends Identifiable> accountableClass, String uuid) {
    String path = resolver.pathOf(accountableClass)//
        .orElseThrow(() -> new RuntimeException("unknown Identifiable class " + accountableClass));
    return UriBuilder.fromUri(config.getBaseURI())//
        .path(path).path("{uuid}").build(uuid);
  }

  public URI locationOf(IdentifiablePointer<? extends Identifiable> annotatablePointer) {
    return locationOf(annotatablePointer.getIdentifiableClass(), annotatablePointer.getIdentifier());
  }
}
