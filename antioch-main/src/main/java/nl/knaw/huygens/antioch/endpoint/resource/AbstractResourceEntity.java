package nl.knaw.huygens.antioch.endpoint.resource;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.api.model.PropertyPrefix;
import nl.knaw.huygens.antioch.endpoint.AbstractAnnotatableEntity;
import nl.knaw.huygens.antioch.model.AntiochResource;

import java.net.URI;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public abstract class AbstractResourceEntity extends AbstractAnnotatableEntity {

  public AbstractResourceEntity() {
    super();
  }

  public Boolean hasText() {
    return getResource().hasText();
  }

  @JsonProperty(PropertyPrefix.LINK + "text")
  public URI getText() {
    return hasText() ? locationBuilder.locationOf(getResource(), EndpointPaths.TEXT) : null;
  }

  @JsonProperty(PropertyPrefix.LINK + "subresources")
  public Set<URI> getSubResources() {
    return getResource().getSubResourcePointers().stream().map(locationBuilder::locationOf).collect(toSet());
  }

  private AntiochResource getResource() {
    return (AntiochResource) getAnnotatable();
  }

}
