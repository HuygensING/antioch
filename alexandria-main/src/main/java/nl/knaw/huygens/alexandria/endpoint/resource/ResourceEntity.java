package nl.knaw.huygens.alexandria.endpoint.resource;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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

import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.alexandria.endpoint.AbstractAnnotatableEntity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

@JsonTypeName("resource")
@JsonPropertyOrder({ "id", "ref" })
@ApiModel("resource")
public class ResourceEntity extends AbstractAnnotatableEntity {

  @JsonIgnore
  private final AlexandriaResource resource;

  public static ResourceEntity of(AlexandriaResource someResource) {
    return new ResourceEntity(someResource);
  }

  private ResourceEntity(AlexandriaResource resource) {
    this.resource = resource;
  }

  public final ResourceEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  public String getRef() {
    return resource.getCargo();
  }

  @JsonProperty(PropertyPrefix.LINK + "subresources")
  public Set<URI> getSubResources() {
    return resource.getSubResourcePointers().stream().map(locationBuilder::locationOf).collect(toSet());
  }

  @Override
  protected AbstractAnnotatable getAnnotatable() {
    return resource;
  }

}
