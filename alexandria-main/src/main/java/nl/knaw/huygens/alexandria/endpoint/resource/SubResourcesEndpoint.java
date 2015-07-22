package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class SubResourcesEndpoint extends JSONEndpoint {

  private final AlexandriaService service;
  private final UUID parentUuid;
  private final ResourceCreationRequestBuilder requestBuilder;
  private final LocationBuilder locationBuilder;

  @Inject
  public SubResourcesEndpoint(AlexandriaService service, ResourceCreationRequestBuilder requestBuilder, LocationBuilder locationBuilder, @PathParam("uuid") final UUIDParam uuidParam) {
    this.locationBuilder = locationBuilder;
    Log.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
    this.service = service;
    this.requestBuilder = requestBuilder;
    this.parentUuid = uuidParam.getValue();
    AlexandriaResource resource = service.readResource(parentUuid)//
        .orElseThrow(ResourcesEndpoint.resourceNotFoundForId(parentUuid));
    if (resource.isTentative()) {
      throw ResourcesEndpoint.resourceIsTentativeException(parentUuid);
    }
  }

  @GET
  @ApiOperation(value = "get subresources", response = ResourceEntity.class)
  public Response get() {
    final Set<AlexandriaResource> subresources = service.readSubResources(parentUuid);
    final Set<SubResourceEntity> outgoing = subresources.stream()//
        .map(sr -> SubResourceEntity.of(sr).withLocationBuilder(locationBuilder))//
        .collect(toSet());
    return Response.ok(outgoing).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("add subresource")
  public Response addSubResource(@NotNull @Valid SubResourcePrototype prototype) {
    prototype.setState(AlexandriaState.TENTATIVE);
    SubResourceCreationRequest request = requestBuilder.build(parentUuid, prototype);
    AlexandriaResource resource = request.execute(service);
    return Response.created(locationBuilder.locationOf(resource)).build();
  }

}
