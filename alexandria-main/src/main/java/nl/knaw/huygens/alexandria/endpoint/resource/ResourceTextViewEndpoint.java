package nl.knaw.huygens.alexandria.endpoint.resource;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.google.common.base.Joiner;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.text.view.TextView;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinition;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewDefinitionParser;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewEntity;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewList;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.ResourceTextFactory;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class ResourceTextViewEndpoint extends JSONEndpoint {
  private LocationBuilder locationBuilder;
  private UUID resourceId;
  private AlexandriaService service;
  private ResourceTextFactory textFactory;

  @Inject
  public ResourceTextViewEndpoint(AlexandriaService service, //
                                  LocationBuilder locationBuilder, //
                                  ResourceTextFactory textFactory, //
                                  ResourceValidatorFactory validatorFactory, //
                                  @PathParam("uuid") final UUIDParam uuidParam) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    this.textFactory = textFactory;
    AlexandriaResource resource = validatorFactory.validateExistingResource(uuidParam).notTentative().get();
    this.resourceId = resource.getId();
  }

  @GET
  public Response getTextViews() {
    TextViewList views = new TextViewList();
    service.getTextViewsForResource(resourceId).stream()//
      .map(this::toTextViewEntity)//
      .forEach(views::add);
    return ok(views);
  }

  @GET
  @Path("{viewId}")
  public Response getTextViewDefinition(@PathParam("viewId") String viewId, @Context UriInfo uriInfo) {
    Map<String, String> viewParameters = ResourceTextEndpoint.getViewParameters(uriInfo);
    TextViewDefinition textView = service.getTextViewDefinition(resourceId, viewId)//
      .orElseThrow(() -> new NotFoundException("No view '" + viewId + "' found for this resource."));
    textView.substitute(viewParameters);
    return ok(textView);
  }

  @PUT
  @Path("{viewId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateTextView(@PathParam("viewId") String viewId, @NotNull TextViewDefinition textViewDefinition) {
    TextViewDefinitionParser textViewDefinitionParser = new TextViewDefinitionParser(textViewDefinition);
    TextView textView = textViewDefinitionParser.getTextView()//
      .orElseThrow(() -> new BadRequestException(Joiner.on("\n").join(textViewDefinitionParser.getErrors())));

    boolean updateView = service.getTextViewDefinition(resourceId, viewId).isPresent();
    service.setTextView(resourceId, viewId, textView, textViewDefinition);
    URI location = locationBuilder.locationOf(AlexandriaResource.class, resourceId, EndpointPaths.TEXT, EndpointPaths.TEXTVIEWS, viewId);
    return updateView //
      ? noContent() //
      : created(location);
  }

  private TextViewEntity toTextViewEntity(TextView textView) {
    return textFactory.createTextViewEntity(resourceId, textView);
  }
}
