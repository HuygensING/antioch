package nl.knaw.huygens.alexandria.endpoint.provenance;

import java.net.URI;
import java.time.Instant;

import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

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

  public static ProvenanceEntity of(AlexandriaProvenance provenance) {
    return new ProvenanceEntity(provenance);
  }

  public final ProvenanceEntity withConfig(AlexandriaConfiguration config) {
    this.config = config;
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

  private ProvenanceEntity(AlexandriaProvenance provenance) {
    this.provenance = provenance;
  }

  private URI accountableURI(Accountable what) {
    // TODO: move this responsibility, instanceof + if/else = smell
    String endpoint = "";
    Object id = null;
    if (what instanceof AlexandriaResource) {
      endpoint = EndpointPaths.RESOURCES;
      id = ((AlexandriaResource) what).getId();

    } else if (what instanceof AlexandriaAnnotation) {
      endpoint = EndpointPaths.ANNOTATIONS;
      id = ((AlexandriaAnnotation) what).getId();

    } else if (what instanceof AlexandriaAnnotationBody) {
      endpoint = EndpointPaths.ANNOTATIONBODIES;
      id = ((AlexandriaAnnotationBody) what).getId();

    } else {
      throw new RuntimeException("Unknown Accountable: " + what.getClass());
    }
    return UriBuilder.fromUri(config.getBaseURI())//
        .path(endpoint).path("{uuid}").build(id);

  }
}
