package nl.knaw.huygens.alexandria.endpoint;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.UUID;
import java.util.function.Supplier;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.model.Identifiable;
import nl.knaw.huygens.alexandria.model.IdentifiablePointer;

public class LocationBuilder {
  private final AlexandriaConfiguration config;
  private final EndpointPathResolver resolver;

  @Inject
  public LocationBuilder(AlexandriaConfiguration config, EndpointPathResolver resolver) {
    this.config = config;
    this.resolver = resolver;
  }

  public URI locationOf(Identifiable identifiable) {
    return locationOf(identifiable.getClass(), identifiable.getId());
  }

  public URI locationOf(Class<? extends Identifiable> identifiableClass, UUID uuid) {
    return locationOf(identifiableClass, uuid.toString());
  }

  public URI locationOf(Class<? extends Identifiable> identifiableClass, String uuid) {
    return UriBuilder.fromUri(config.getBaseURI()) //
        .path(pathOf(identifiableClass)) //
        .path("{uuid}") //
        .build(uuid);
  }

  public URI locationOf(IdentifiablePointer<? extends Identifiable> identifiablePointer) {
    return locationOf(identifiablePointer.getIdentifiableClass(), identifiablePointer.getIdentifier());
  }

  private String pathOf(Class<? extends Identifiable> identifiableClass) {
    return resolver.pathOf(identifiableClass).orElseThrow(unknownIdentifiableClass(identifiableClass));
  }

  private Supplier<RuntimeException> unknownIdentifiableClass(Class<? extends Identifiable> identifiableClass) {
    return () -> new RuntimeException("unknown Identifiable class " + identifiableClass);
  }
}
