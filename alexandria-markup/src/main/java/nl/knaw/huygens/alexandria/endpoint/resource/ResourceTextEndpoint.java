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

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.ProcessStatusMap;
import nl.knaw.huygens.alexandria.api.model.text.ResourceTextFactory;
import nl.knaw.huygens.alexandria.api.model.text.TextAnnotationImportStatus;
import nl.knaw.huygens.alexandria.api.model.text.TextEntity;
import nl.knaw.huygens.alexandria.api.model.text.TextImportStatus;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationList;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewEntity;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.ConflictException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.MarkupService;
import nl.knaw.huygens.alexandria.text.TextPrototype;
import nl.knaw.huygens.alexandria.textgraph.DotFactory;
import nl.knaw.huygens.alexandria.textgraph.TextGraphImportTask;
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil;
import nl.knaw.huygens.alexandria.textgraph.TextRangeAnnotationValidatorFactory;

public class ResourceTextEndpoint extends JSONEndpoint {
  private final MarkupService service;
  private final UUID resourceUUID;
  private final AlexandriaResource resource;
  private ExecutorService executorService;
  private final LocationBuilder locationBuilder;
  private final ProcessStatusMap<TextImportStatus> taskStatusMap;
  private final ProcessStatusMap<TextAnnotationImportStatus> annotationTaskStatusMap;
  private AlexandriaConfiguration config;
  private ResourceTextFactory textFactory;
  private TextRangeAnnotationValidatorFactory textRangeAnnotationValidator;

  @Inject
  public ResourceTextEndpoint(MarkupService service, //
                              AlexandriaConfiguration config, //
                              ResourceValidatorFactory validatorFactory, //
                              ExecutorService executorService, //
                              LocationBuilder locationBuilder, //
                              ResourceTextFactory resourceTextFactory, //
                              ProcessStatusMap<TextImportStatus> taskStatusMap, //
                              ProcessStatusMap<TextAnnotationImportStatus> annotationTaskStatusMap, //
                              @PathParam("uuid") final UUIDParam uuidParam) {
    this.service = service;
    this.config = config;
    this.executorService = executorService;
    this.locationBuilder = locationBuilder;
    this.textFactory = resourceTextFactory;
    this.taskStatusMap = taskStatusMap;
    this.annotationTaskStatusMap = annotationTaskStatusMap;
    this.resource = validatorFactory.validateExistingResource(uuidParam).notTentative().get();
    this.resourceUUID = resource.getId();
    this.textRangeAnnotationValidator = new TextRangeAnnotationValidatorFactory(service, resourceUUID);
  }

  @PUT
  @Consumes(MediaType.TEXT_XML)
  @ApiOperation("set text from xml")
  public Response setTextFromXml(@NotNull @Valid String xml) {
    assertResourceHasNoText();
    startTextProcessing(xml);
    return Response.accepted()//
      .location(locationBuilder.locationOf(resource, EndpointPaths.TEXT, "status"))//
      .build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("set text from text prototype")
  public Response setTextWithPrototype(@NotNull @Valid TextPrototype prototype) {
    String body = prototype.getBody();
    return setTextFromXml(body);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  @ApiOperation("set text from stream")
  public Response setTextFromXmlStream(InputStream inputStream) {
    try {
      String xml = IOUtils.toString(inputStream, Charsets.UTF_8);
      return setTextFromXml(xml);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @GET
  public Response getTextEntity() {
    assertResourceHasText();
    List<TextViewEntity> textViewEntities = service.getTextViewsForResource(resourceUUID)//
      .stream()//
      .map(tv -> textFactory.createTextViewEntity(resourceUUID, tv))//
      .collect(toList());
    TextEntity text = textFactory.createTextEntity(resourceUUID, textViewEntities);
    return ok(text);
  }

  @Path(EndpointPaths.TEXTVIEWS)
  public Class<ResourceTextViewEndpoint> getTextViewEndpoint() {
    return ResourceTextViewEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

  @GET
  @Path("status")
  public Response getTextGraphImportStatus() {
    TextImportStatus textGraphImportTaskStatus = taskStatusMap.get(resourceUUID)//
      .orElseThrow(NotFoundException::new);
    return ok(textGraphImportTaskStatus);
  }

  @GET
  @Path("xml")
  @Produces(MediaType.TEXT_XML)
  @ApiOperation("get textgraph as xml")
  public Response getXML(@QueryParam("view") String viewName, @Context UriInfo uriInfo) {
    assertResourceHasText();
    Map<String, String> viewParameters = getViewParameters(uriInfo);
    StreamingOutput outputstream = TextGraphUtil.xmlOutputStream(service, resourceUUID, viewName, viewParameters);
    return ok(outputstream);
  }

  static Map<String, String> getViewParameters(UriInfo uriInfo) {
    MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
    Map<String, String> viewParameters = new HashMap<>();
    queryParameters.forEach((k, v) -> {
      if (v.size() > 1) {
        throw new BadRequestException("view parameter '" + k + "' has multiple values, should be single-valued");
      }
      if (k.startsWith("view.")) {
        viewParameters.put(k.replace("view.", ""), v.get(0));
      }
    });
    return viewParameters;
  }

  @GET
  @Path("dot")
  @Produces(MediaType.TEXT_PLAIN + "; charSet=utf-8")
  @ApiOperation("get textgraph as .dot output")
  public Response getDot() {
    assertResourceHasText();
    String dot = DotFactory.createDot(service, resourceUUID);
    return ok(dot);
  }

  @Path(EndpointPaths.ANNOTATIONS)
  public Class<ResourceTextAnnotationEndpoint> getResourceTextAnnotationEndpoint(@PathParam("uuid") final UUIDParam uuidParam) {
    return ResourceTextAnnotationEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

  @POST
  @Path(EndpointPaths.ANNOTATIONBATCH)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response postAnnotationList(@NotNull TextRangeAnnotationList newTextRangeAnnotationList) {
    startAnnotating(newTextRangeAnnotationList);
    return Response.accepted()//
      .location(locationBuilder.locationOf(resource, EndpointPaths.TEXT, EndpointPaths.ANNOTATIONBATCH, "status"))//
      .build();
  }

  @GET
  @Path(EndpointPaths.ANNOTATIONBATCH + "/status")
  public Response getAnnotationBatchStatus() {
    TextAnnotationImportStatus textAnnotationsImportTaskStatus = annotationTaskStatusMap.get(resourceUUID)//
      .orElseThrow(NotFoundException::new);
    return ok(textAnnotationsImportTaskStatus);
  }

  /* private methods */

  private void startTextProcessing(String xml) {
    TextGraphImportTask task = new TextGraphImportTask(service, locationBuilder, xml, resource);
    taskStatusMap.put(resource.getId(), task.getStatus());
    runTask(task);
  }

  private void startAnnotating(TextRangeAnnotationList newTextRangeAnnotationList) {
    TextAnnotationBatchTask task = new TextAnnotationBatchTask(service, newTextRangeAnnotationList, textRangeAnnotationValidator, resource);
    annotationTaskStatusMap.put(resource.getId(), task.getStatus());
    runTask(task);
  }

  private void runTask(Runnable task) {
    if (config.asynchronousEndpointsAllowed()) {
      executorService.execute(task);
    } else {
      // For now, for the acceptance tests.
      task.run();
    }
  }

  private void assertResourceHasText() {
    if (!resource.hasText()) {
      throw new NotFoundException("this resource has no text");
    }
  }

  private void assertResourceHasNoText() {
    if (resource.hasText()) {
      throw new ConflictException("This resource already has a text, which cannot be replaced.");
    }
  }

}
