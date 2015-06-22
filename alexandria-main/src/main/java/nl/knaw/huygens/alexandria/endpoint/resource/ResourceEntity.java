package nl.knaw.huygens.alexandria.endpoint.resource;

import io.swagger.annotations.ApiModel;

import java.net.URI;
import java.util.UUID;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.AnnotatableObjectEntity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("resource")
@ApiModel("resource")
class ResourceEntity extends AnnotatableObjectEntity {

  @JsonIgnore
  private final AlexandriaResource resource;

  @JsonIgnore
  private AlexandriaConfiguration config;

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

  public UUID getId() {
    return resource.getId();
  }

  public String getRef() {
    return resource.getRef();
  }

  @JsonProperty(PropertyPrefix.LINK + "provenance")
  public URI getProvenance() {
    return URI.create(locationBuilder.locationOf(resource) + "/provenance");
  }

  @Override
  protected AbstractAnnotatable getAnnotatable() {
    return resource;
  }

}
