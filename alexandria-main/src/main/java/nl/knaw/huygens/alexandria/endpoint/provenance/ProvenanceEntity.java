package nl.knaw.huygens.alexandria.endpoint.provenance;

import java.net.URI;
import java.time.Instant;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
  private AlexandriaConfiguration config;

  @JsonIgnore
  private LocationBuilder locationBuilder;

  private ProvenanceEntity(AlexandriaProvenance provenance) {
    this.provenance = provenance;
  }

  public static ProvenanceEntity of(AlexandriaProvenance provenance) {
    return new ProvenanceEntity(provenance);
  }

  public final ProvenanceEntity withConfig(AlexandriaConfiguration config) {
    this.config = config;
    return this;
  }

  public ProvenanceEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  public String getWho() {
    return provenance.getWho();
  }

  public URI getWhat() {
    return locationBuilder.locationOf(provenance.getWhat());
  }

  public Instant getWhen() {
    return provenance.getWhen();
  }

  public String getWhy() {
    return provenance.getWhy();
  }

}
