package nl.knaw.huygens.alexandria.endpoint;

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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.net.URI;

import nl.knaw.huygens.alexandria.endpoint.resource.PropertyPrefix;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("provenance")
@ApiModel("provenance")
public class ProvenanceEntity extends JsonWrapperObject implements Entity {
  @JsonIgnore
  private final AlexandriaProvenance provenance;

  @JsonIgnore
  private LocationBuilder locationBuilder;

  private ProvenanceEntity(AlexandriaProvenance provenance) {
    this.provenance = provenance;
  }

  public static ProvenanceEntity of(AlexandriaProvenance provenance) {
    return new ProvenanceEntity(provenance);
  }

  public ProvenanceEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  @ApiModelProperty(notes = "who made the what")
  public String getWho() {
    return provenance.getWho();
  }

  @JsonProperty(PropertyPrefix.LINK + "what")
  @ApiModelProperty(notes = "URI of the object that the provenance pertains to")
  public URI getWhat() {
    return locationBuilder.locationOf(provenance.getWhat());
  }

  @ApiModelProperty(notes = "when was the what made")
  public String getWhen() {
    return provenance.getWhen().toString();
  }

  @ApiModelProperty(notes = "reason for making the what")
  public String getWhy() {
    return provenance.getWhy();
  }

}
