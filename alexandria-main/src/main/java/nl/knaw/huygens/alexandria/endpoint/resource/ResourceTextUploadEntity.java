package nl.knaw.huygens.alexandria.endpoint.resource;

import java.net.URI;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.alexandria.endpoint.Entity;
import nl.knaw.huygens.alexandria.endpoint.JsonWrapperObject;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

@JsonTypeName("resourceTextUploadResult")
@JsonInclude(Include.NON_NULL)
@ApiModel("ResourceTextUploadresult")
public class ResourceTextUploadEntity extends JsonWrapperObject implements Entity {

  @JsonIgnore
  protected LocationBuilder locationBuilder;

  @JsonIgnore
  private UUID baseLayerDefiningResourceId;

  private ResourceTextUploadEntity(UUID baseLayerDefiningResourceId) {
    this.baseLayerDefiningResourceId = baseLayerDefiningResourceId;
  }

  public final ResourceTextUploadEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  public static ResourceTextUploadEntity of(UUID baseLayerDefiningResourceId) {
    return new ResourceTextUploadEntity(baseLayerDefiningResourceId);
  }

  @JsonProperty(PropertyPrefix.LINK + "baseLayerDefinition")
  public URI getBaseLayerDefinitionURI() {
    return locationBuilder.locationOf(AlexandriaResource.class, baseLayerDefiningResourceId, ResourcesEndpoint.BASELAYERDEFINITION);
  }

}
