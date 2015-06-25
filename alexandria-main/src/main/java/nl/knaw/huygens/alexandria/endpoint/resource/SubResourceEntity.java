package nl.knaw.huygens.alexandria.endpoint.resource;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.alexandria.endpoint.AbstractAnnotatableEntity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaSubResource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("subresource")
@ApiModel("subresource")
public class SubResourceEntity extends AbstractAnnotatableEntity {

  @JsonIgnore
  private final AlexandriaSubResource subResource;

  public static SubResourceEntity of(AlexandriaSubResource someSubResource) {
    return new SubResourceEntity(someSubResource);
  }

  private SubResourceEntity(AlexandriaSubResource subresource) {
    this.subResource = subresource;
  }

  public final SubResourceEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  public String getSub() {
    return subResource.getSub();
  }

  @JsonProperty(PropertyPrefix.LINK + "part_of")
  public String getParentResource() {
    return locationBuilder.locationOf(subResource.getParentResourcePointer()).toString();
  }

  @Override
  protected AbstractAnnotatable getAnnotatable() {
    return subResource;
  }

}
