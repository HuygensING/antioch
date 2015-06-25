package nl.knaw.huygens.alexandria.endpoint;

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
