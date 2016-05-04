package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.DeprecatedTextView;
import nl.knaw.huygens.alexandria.api.model.TextViewPrototype;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class ResourceTextViewEndpoint extends JSONEndpoint {
  private LocationBuilder locationBuilder;
  private UUID resourceId;
  private AlexandriaService service;

  @Inject
  public ResourceTextViewEndpoint(AlexandriaService service, //
      LocationBuilder locationBuilder, //
      ResourceValidatorFactory validatorFactory, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    AlexandriaResource resource = validatorFactory.validateExistingResource(uuidParam).notTentative();
    this.resourceId = resource.getId();
  }

  @GET
  public Response getTextViews() {
    List<TextViewEntity> views = service.getTextViewsForResource(resourceId).stream()//
        .map(this::toTextViewEntity)//
        .collect(toList());
    return ok(views);
  }

  @GET
  @Path("{viewId}")
  public Response getTextView(@PathParam("viewId") String viewId) {
    DeprecatedTextView textView = service.getTextView(resourceId, viewId)//
        .orElseThrow(() -> new NotFoundException("No view '" + viewId + "' found for this resource."));
    return ok(textView);
  }

  @PUT
  @Path("{viewId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateTextView(@PathParam("viewId") String viewId, @NotNull TextViewPrototype protoType) {
    Log.trace("protoType=[{}]", protoType);
    if (!protoType.isValid()) {
      throw new BadRequestException("Setting both includedElements and excludedElements is not allowed.");
    }
    boolean updateView = service.getTextView(resourceId, viewId).isPresent();
    service.setTextView(resourceId, viewId, protoType);
    URI location = locationBuilder.locationOf(AlexandriaResource.class, resourceId, EndpointPaths.TEXT, EndpointPaths.TEXTVIEWS, viewId);
    return updateView //
        ? noContent() //
        : created(location);
  }

  private TextViewEntity toTextViewEntity(DeprecatedTextView textView) {
    return new TextViewEntity(resourceId, textView, locationBuilder);
  }
}
