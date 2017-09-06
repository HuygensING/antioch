package nl.knaw.huygens.antioch.endpoint;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import nl.knaw.huygens.antioch.api.JsonTypeNames;
import nl.knaw.huygens.antioch.api.model.Entity;
import nl.knaw.huygens.antioch.api.model.JsonWrapperObject;
import nl.knaw.huygens.antioch.api.model.PropertyPrefix;
import nl.knaw.huygens.antioch.model.AntiochProvenance;

@JsonTypeName(JsonTypeNames.PROVENANCE)
@ApiModel("provenance")
public class ProvenanceEntity extends JsonWrapperObject implements Entity {
  @JsonIgnore
  private final AntiochProvenance provenance;

  @JsonIgnore
  private LocationBuilder locationBuilder;

  private ProvenanceEntity(AntiochProvenance provenance) {
    this.provenance = provenance;
  }

  public static ProvenanceEntity of(AntiochProvenance provenance) {
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
