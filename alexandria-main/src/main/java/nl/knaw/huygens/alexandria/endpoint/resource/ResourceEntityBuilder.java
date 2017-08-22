package nl.knaw.huygens.alexandria.endpoint.resource;

/*
 * #%L
 * alexandria-main
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

import nl.knaw.huygens.alexandria.endpoint.AbstractAnnotatableEntity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public class ResourceEntityBuilder {

  private final LocationBuilder locationBuilder;

  @Inject
  public ResourceEntityBuilder(LocationBuilder locationBuilder) {
    // Log.trace("ResourceCreationRequestBuilder created: locationBuilder=[{}]", locationBuilder);
    this.locationBuilder = locationBuilder;
  }

  public AbstractAnnotatableEntity build(AlexandriaResource resource) {
    return resource.isSubResource()//
        ? SubResourceEntity.of(resource).withLocationBuilder(locationBuilder) //
        : ResourceEntity.of(resource).withLocationBuilder(locationBuilder);
  }
}
