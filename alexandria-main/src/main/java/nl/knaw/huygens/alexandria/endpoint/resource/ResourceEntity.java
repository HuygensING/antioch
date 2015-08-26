package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.alexandria.endpoint.AbstractAnnotatableEntity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

@JsonTypeName("resource")
@JsonPropertyOrder({ "id", "ref" })
@ApiModel("resource")
public class ResourceEntity extends AbstractAnnotatableEntity {

  @JsonIgnore
  private final AlexandriaResource resource;

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

  public String getRef() {
    return resource.getCargo();
  }

  @JsonProperty(PropertyPrefix.LINK + "subresources")
  public Set<URI> getSubResources() {
    return resource.getSubResourcePointers().stream().map(locationBuilder::locationOf).collect(toSet());
  }

  @Override
  protected AbstractAnnotatable getAnnotatable() {
    return resource;
  }

}
