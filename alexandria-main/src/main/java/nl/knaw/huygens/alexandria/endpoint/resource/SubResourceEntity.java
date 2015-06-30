package nl.knaw.huygens.alexandria.endpoint.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.alexandria.endpoint.AbstractAnnotatableEntity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

@JsonTypeName("subresource")
@ApiModel("subresource")
public class SubResourceEntity extends AbstractAnnotatableEntity {

  @JsonIgnore
  private final AlexandriaResource subResource;

  public static SubResourceEntity of(AlexandriaResource someSubResource) {
    return new SubResourceEntity(someSubResource);
  }

  private SubResourceEntity(AlexandriaResource subresource) {
    this.subResource = subresource;
  }

  public final SubResourceEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  public String getSub() {
    return subResource.getCargo();
  }

  @JsonProperty(PropertyPrefix.LINK + "part_of")
  public String getParentResource() {
    return locationBuilder.locationOf(subResource.getParentResourcePointer().get()).toString();
  }

  @Override
  protected AbstractAnnotatable getAnnotatable() {
    return subResource;
  }

}
