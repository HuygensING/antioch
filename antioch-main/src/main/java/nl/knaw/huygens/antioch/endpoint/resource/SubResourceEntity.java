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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.antioch.api.JsonTypeNames;
import nl.knaw.huygens.antioch.api.model.PropertyPrefix;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.model.AbstractAnnotatable;
import nl.knaw.huygens.antioch.model.AntiochResource;

@JsonTypeName(JsonTypeNames.SUBRESOURCE)
@JsonInclude(Include.NON_NULL)
@ApiModel("subresource")
public class SubResourceEntity extends AbstractResourceEntity {

  @JsonIgnore
  private final AntiochResource subResource;

  public static SubResourceEntity of(AntiochResource someSubResource) {
    return new SubResourceEntity(someSubResource);
  }

  private SubResourceEntity(AntiochResource subresource) {
    this.subResource = subresource;
  }

  public final SubResourceEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  public String getSub() {
    return subResource.getCargo();
  }

  @JsonProperty(PropertyPrefix.LINK + "partOf")
  public String getParentResource() {
    return locationBuilder.locationOf(subResource.getParentResourcePointer().get()).toString();
  }

  @Override
  protected AbstractAnnotatable getAnnotatable() {
    return subResource;
  }

}
