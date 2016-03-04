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

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.Entity;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;

public abstract class AbstractAnnotatableEntity extends JsonWrapperObject implements Entity {

  @JsonIgnore
  protected LocationBuilder locationBuilder;

  abstract protected AbstractAnnotatable getAnnotatable();

  @JsonProperty(PropertyPrefix.LINK + "annotations")
  public Set<URI> getAnnotations() {
    Log.debug("Converting {} annotations: [{}]", getAnnotatable().getAnnotations().size(), getAnnotatable().getAnnotations());
    // TODO: When Jackson can handle Streams, maybe return Stream<AnnotationView>.
    final Set<URI> uris = Sets.newHashSet(getAnnotatable().getAnnotations().stream().map(this::annotationURI).iterator());
    Log.debug("uris: {}", uris);
    return uris;
  }

  private URI annotationURI(AlexandriaAnnotation annotation) {
    Log.debug("annotationURI for: [{}], id=[{}]", annotation, annotation.getId());
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
