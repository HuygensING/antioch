package nl.knaw.huygens.alexandria.endpoint;

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

import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.search.SearchResult;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.Identifiable;

@Singleton
public class EndpointPathResolver {
  private static final Map<Class<? extends Identifiable>, String> IDENTIFIABLE_ENDPOINTS //
      = ImmutableMap.<Class<? extends Identifiable>, String>builder()
      .put(AlexandriaAnnotation.class, EndpointPaths.ANNOTATIONS)
      .put(AlexandriaAnnotationBody.class, EndpointPaths.ANNOTATIONBODIES)
      .put(AlexandriaResource.class, EndpointPaths.RESOURCES)
      .put(SearchResult.class, EndpointPaths.SEARCHES)
      .build();

  public Optional<String> pathOf(Class<? extends Identifiable> identifiableClass) {
    return Optional.ofNullable(IDENTIFIABLE_ENDPOINTS.get(identifiableClass));
  }

}
