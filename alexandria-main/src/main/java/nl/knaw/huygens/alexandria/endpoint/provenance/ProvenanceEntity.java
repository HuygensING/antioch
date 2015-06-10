package nl.knaw.huygens.alexandria.endpoint.provenance;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("provenance")
public class ProvenanceEntity {
  @JsonIgnore
  private final AlexandriaProvenance provenance;

  @JsonIgnore
  private AlexandriaConfiguration config;

  @JsonIgnore
  private EndpointPathResolver resolver;

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

  public ProvenanceEntity withResolver(EndpointPathResolver resolver) {
    this.resolver = resolver;
    return this;
  }

  public String getWho() {
    return provenance.getWho();
  }

  public URI getWhat() {
    return accountableURI(provenance.getWhat());
  }

  public Instant getWhen() {
    return provenance.getWhen();
  }

  public String getWhy() {
    return provenance.getWhy();
  }

  private URI accountableURI(Accountable what) {
    final String endpoint = resolver.pathOf(what);
    return UriBuilder.fromUri(config.getBaseURI()).path(endpoint).path("{uuid}").build(what.getId());
  }
}
