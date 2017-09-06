package nl.knaw.huygens.antioch.endpoint;

import java.net.URI;
import java.util.UUID;
import java.util.function.Supplier;

/*
 * #%L
 * antioch-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.config.AntiochConfiguration;
import nl.knaw.huygens.antioch.model.Identifiable;
import nl.knaw.huygens.antioch.model.IdentifiablePointer;

public class LocationBuilder {
  private final AntiochConfiguration config;
  private final EndpointPathResolver resolver;

  @Inject
  public LocationBuilder(AntiochConfiguration config, EndpointPathResolver resolver) {
    this.config = config;
    this.resolver = resolver;
  }

  public URI locationOf(Identifiable identifiable, String... subPaths) {
    return locationOf(identifiable.getClass(), identifiable.getId(), subPaths);
  }

  private URI locationOf(Class<? extends Identifiable> identifiableClass, UUID uuid, String... subPaths) {
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
