package nl.knaw.huygens.alexandria.endpoint;

import java.net.URI;

import nl.knaw.huygens.alexandria.endpoint.resource.PropertyPrefix;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("provenance")
public class ProvenanceEntity {
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

  public String getWho() {
    return provenance.getWho();
  }

  @JsonProperty(PropertyPrefix.LINK + "what")
  public URI getWhat() {
    return locationBuilder.locationOf(provenance.getWhat());
  }

  public String getWhen() {
    return provenance.getWhen().toString();
  }

  public String getWhy() {
    return provenance.getWhy();
  }

}
