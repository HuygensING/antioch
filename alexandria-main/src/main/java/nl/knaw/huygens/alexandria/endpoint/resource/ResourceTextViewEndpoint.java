package nl.knaw.huygens.alexandria.endpoint.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.TextViewPrototype;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public class ResourceTextViewEndpoint {
  private LocationBuilder locationBuilder;
  private UUID resourceId;

  @Inject
  public ResourceTextViewEndpoint(LocationBuilder locationBuilder, //
      ResourceValidatorFactory validatorFactory, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    this.locationBuilder = locationBuilder;
    AlexandriaResource resource = validatorFactory.validateExistingResource(uuidParam).notTentative();
    this.resourceId = resource.getId();
  }

  @GET
  public Response getTextViews() {
    List<TextViewEntity> views = new ArrayList<>();
    // TODO: implement!
    return Response.ok().entity(views).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response addTextView(@NotNull TextViewPrototype protoType) {
    Log.trace("protoType=[{}]", protoType);
    URI uri = null;
    // TODO: implement!
    return Response.created(uri).build();
  }

  @GET
  @Path("{viewId}")
  public Response getTextView(@PathParam("viewId") String viewId) {
    TextViewEntity view = new TextViewEntity(resourceId, viewId, locationBuilder);
    // TODO: implement!
    return Response.ok().entity(view).build();
  }

  @PUT
  @Path("{viewId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateTextView(@PathParam("viewId") String viewId, @NotNull TextViewPrototype protoType) {
    Log.trace("protoType=[{}]", protoType);
    TextViewEntity view = new TextViewEntity(resourceId, viewId, locationBuilder);

    // TODO: implement!
    return Response.ok().entity(view).build();
  }

  // @PUT
  // @Path("{uuid}/" + EndpointPaths.BASELAYERDEFINITION)
  // @Consumes(MediaType.APPLICATION_JSON)
  // @ApiOperation(value = "Set the baselayer definition")
  // public Response setBaseLayerDefinition(@PathParam("uuid") final UUIDParam uuidParam, @NotNull TextViewPrototype protoType) {
  // Log.trace("protoType=[{}]", protoType);
  // AlexandriaResource resource = readExistingResource(uuidParam);
  // if (!isConfirmed(resource)) {
  // throw new ConflictException("This resource has state " + resource.getState() + "; it needs to be CONFIRMED before the BaseLayerDefinition can be added.");
  // }
  // if (resource.getDirectBaseLayerDefinition().isPresent()) {
  // throw new ConflictException("This resource already has a baselayer definition");
  // }
  // service.setBaseLayerDefinition(uuidParam.getValue(), protoType);
  // return created(locationBuilder.locationOf(resource, EndpointPaths.BASELAYERDEFINITION));
  // }

  // @GET
  // @Path("{uuid}/" + EndpointPaths.BASELAYERDEFINITION)
  // @ApiOperation(value = "Get the baselayer definition")
  // public Response getBaseLayerDefinition(@PathParam("uuid") final UUIDParam uuidParam) {
  // AlexandriaResource resource = readExistingResource(uuidParam);
  // if (!resource.getDirectBaseLayerDefinition().isPresent()) {
  // throw new NotFoundException("This resource has no baselayer definition"); // TODO: alternatively, throw redirected to ancestor baselayer definition (if any)
  // }
  // return ok(resource.getDirectBaseLayerDefinition().get());
  // }

}
