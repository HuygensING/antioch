package nl.knaw.huygens.alexandria.endpoint.resource;

import static nl.knaw.huygens.alexandria.endpoint.EndpointPaths.RESOURCES;

import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class SubResources extends JSONEndpoint {

  private final AlexandriaService service;

  private final UUID uuid;

  private final AlexandriaConfiguration configuration;

  private final ResourceCreationRequestBuilder requestBuilder;

  @Inject
  public SubResources(AlexandriaService service, //
      ResourceCreationRequestBuilder requestBuilder, //
      AlexandriaConfiguration configuration, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    Log.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
    this.service = service;
    this.requestBuilder = requestBuilder;
    this.configuration = configuration;
    this.uuid = uuidParam.getValue();
  }

  @GET
  public Response get() {
    final Set<AlexandriaResource> subresources = service.readSubResources(uuid);
    final Set<ResourceEntity> outgoing = subresources.stream().map(ResourceEntity::of).collect(Collectors.toSet());
    return Response.ok(outgoing).build();
  }

  @POST
  public Response addSubResource(@NotNull @Valid SubResourcePrototype prototype) {
    ResourceCreationRequest request = requestBuilder.build(uuid, prototype);
    request.execute(service);
    return Response.created(locationOf(uuid)).build();
  }

  // TODO: replace by injected LocationBuilder (to be written) ?
  private URI locationOf(UUID uuid) {
    return UriBuilder.fromUri(configuration.getBaseURI()).path(RESOURCES).path("{uuid}").build(uuid);
  }

}
