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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.antioch.api.JsonTypeNames;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.model.AbstractAnnotatable;
import nl.knaw.huygens.antioch.model.AntiochResource;

@JsonTypeName(JsonTypeNames.RESOURCE)
@JsonPropertyOrder({ "id", "ref", "hasText" })
@JsonInclude(Include.NON_NULL)
@ApiModel("resource")
public class ResourceEntity extends AbstractResourceEntity {

  @JsonIgnore
  private final AntiochResource resource;

  public static ResourceEntity of(AntiochResource someResource) {
    return new ResourceEntity(someResource);
  }

  private ResourceEntity(AntiochResource resource) {
    this.resource = resource;
  }

  public final ResourceEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  public String getRef() {
    return resource.getCargo();
  }

  @Override
  protected AbstractAnnotatable getAnnotatable() {
    return resource;
  }

}
