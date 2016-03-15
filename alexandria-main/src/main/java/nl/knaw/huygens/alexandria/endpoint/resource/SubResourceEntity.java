package nl.knaw.huygens.alexandria.endpoint.resource;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

@JsonTypeName("subresource")
@JsonInclude(Include.NON_NULL)
@ApiModel("subresource")
public class SubResourceEntity extends AbstractResourceEntity {

  @JsonIgnore
  private final AlexandriaResource subResource;

  public static SubResourceEntity of(AlexandriaResource someSubResource) {
    return new SubResourceEntity(someSubResource);
  }

  private SubResourceEntity(AlexandriaResource subresource) {
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
