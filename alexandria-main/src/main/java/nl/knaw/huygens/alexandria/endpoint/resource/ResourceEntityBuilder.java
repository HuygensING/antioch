package nl.knaw.huygens.alexandria.endpoint.resource;

import javax.inject.Inject;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.AbstractAnnotatableEntity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public class ResourceEntityBuilder {

  private final LocationBuilder locationBuilder;

  @Inject
  public ResourceEntityBuilder(LocationBuilder locationBuilder) {
    Log.trace("ResourceCreationRequestBuilder created: locationBuilder=[{}]", locationBuilder);
    this.locationBuilder = locationBuilder;
  }

  public AbstractAnnotatableEntity build(AlexandriaResource resource) {
    return resource.isSubResource() ? SubResourceEntity.of(resource).withLocationBuilder(locationBuilder) //
        : ResourceEntity.of(resource).withLocationBuilder(locationBuilder);
  }
}
