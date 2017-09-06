package nl.knaw.huygens.antioch.endpoint;

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

import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.endpoint.search.SearchResult;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.model.Identifiable;

@Singleton
public class EndpointPathResolver {
  private static final Map<Class<? extends Identifiable>, String> IDENTIFIABLE_ENDPOINTS //
      = ImmutableMap.<Class<? extends Identifiable>, String>builder()
      .put(AntiochAnnotation.class, EndpointPaths.ANNOTATIONS)
      .put(AntiochAnnotationBody.class, EndpointPaths.ANNOTATIONBODIES)
      .put(AntiochResource.class, EndpointPaths.RESOURCES)
      .put(SearchResult.class, EndpointPaths.SEARCHES)
      .build();

  public Optional<String> pathOf(Class<? extends Identifiable> identifiableClass) {
    return Optional.ofNullable(IDENTIFIABLE_ENDPOINTS.get(identifiableClass));
  }

}
