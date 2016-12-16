package nl.knaw.huygens.alexandria.endpoint;

import java.net.URI;
import java.util.UUID;
import java.util.function.Supplier;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
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

  public URI locationOf(Identifiable identifiable, String... subPaths) {
    return locationOf(identifiable.getClass(), identifiable.getId(), subPaths);
  }

  public URI locationOf(Class<? extends Identifiable> identifiableClass, UUID uuid, String... subPaths) {
    return locationOf(identifiableClass, uuid.toString(), subPaths);
  }

  public URI locationOf(IdentifiablePointer<? extends Identifiable> identifiablePointer, String... subPaths) {
    return locationOf(identifiablePointer.getIdentifiableClass(), identifiablePointer.getIdentifier(), subPaths);
  }

  public URI locationOf(Class<? extends Identifiable> identifiableClass, String uuid, String... subPaths) {
    if (uuid.contains(".")) {
      // Special case: uuid of deprecated annotation
      String[] parts = uuid.split("\\.");
      UriBuilder uriBuilder = UriBuilder.fromUri(config.getBaseURI()) //
          .path(pathOf(identifiableClass)) //
          .path("{uuid}") //
          .path(EndpointPaths.REV) //
          .path("{rev}");
      for (String subPath : subPaths) {
        uriBuilder = uriBuilder.path(subPath);
      }
      return uriBuilder //
          .build(parts, true);
    }

    UriBuilder uriBuilder = UriBuilder.fromUri(config.getBaseURI()) //
        .path(pathOf(identifiableClass)) //
        .path("{uuid}");
    for (String subPath : subPaths) {
      uriBuilder = uriBuilder.path(subPath);
    }
    return uriBuilder.build(uuid);
  }

  public URI locationOf(Object... subPaths) {
    UriBuilder uriBuilder = UriBuilder.fromUri(config.getBaseURI());
    // Log.info("subPaths.size={}", subPaths.length);
    for (Object subPath : subPaths) {
      // Log.info("subPath=[{}]", subPath);
      uriBuilder = uriBuilder.path(subPath.toString());
    }
    return uriBuilder.build("X");
  }

  // -- private methods --//

  private String pathOf(Class<? extends Identifiable> identifiableClass, String... subPaths) {
    return resolver.pathOf(identifiableClass).orElseThrow(unknownIdentifiableClass(identifiableClass));
  }

  private Supplier<RuntimeException> unknownIdentifiableClass(Class<? extends Identifiable> identifiableClass) {
    return () -> new RuntimeException("unknown Identifiable class " + identifiableClass);
  }
}
