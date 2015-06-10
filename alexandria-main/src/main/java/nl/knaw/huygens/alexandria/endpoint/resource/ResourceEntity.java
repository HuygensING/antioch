package nl.knaw.huygens.alexandria.endpoint.resource;

import java.util.UUID;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.AnnotatableObjectEntity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AnnotatableObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonTypeName("resource")
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

  public String getCreatedOn() {
    return resource.getProvenance().getWhen().toString(); // ISO-8601 representation
  }

  @Override
  protected AnnotatableObject getAnnotatable() {
    return resource;
  }

}
