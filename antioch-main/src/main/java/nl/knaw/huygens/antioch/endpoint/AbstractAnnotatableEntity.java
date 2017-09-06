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

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import nl.knaw.huygens.antioch.api.model.Entity;
import nl.knaw.huygens.antioch.api.model.JsonWrapperObject;
import nl.knaw.huygens.antioch.api.model.PropertyPrefix;
import nl.knaw.huygens.antioch.model.AbstractAnnotatable;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;

public abstract class AbstractAnnotatableEntity extends JsonWrapperObject implements Entity {

  @JsonIgnore
  protected LocationBuilder locationBuilder;

  abstract protected AbstractAnnotatable getAnnotatable();

  @JsonProperty(PropertyPrefix.LINK + "annotations")
  public Set<URI> getAnnotations() {
    // Log.debug("Converting {} annotations: [{}]", getAnnotatable().getAnnotations().size(), getAnnotatable().getAnnotations());
    // TODO: When Jackson can handle Streams, maybe return Stream<AnnotationView>.
    final Set<URI> uris = Sets.newHashSet(getAnnotatable().getAnnotations().stream().map(this::annotationURI).iterator());
    // Log.debug("uris: {}", uris);
    return uris;
  }

  private URI annotationURI(AntiochAnnotation annotation) {
    // Log.debug("annotationURI for: [{}], id=[{}]", annotation, annotation.getId());
    return locationBuilder.locationOf(annotation);
  }

  @JsonProperty(PropertyPrefix.LINK + "provenance")
  public URI getProvenance() {
    return locationBuilder.locationOf(getAnnotatable(), "provenance");
  }

  public UUID getId() {
    return getAnnotatable().getId();
  }

  public Map<String, Object> getState() {
    return ImmutableMap.of(//
        "value", getAnnotatable().getState(), //
        "since", getAnnotatable().getStateSince().toString()//
    );
  }
}
