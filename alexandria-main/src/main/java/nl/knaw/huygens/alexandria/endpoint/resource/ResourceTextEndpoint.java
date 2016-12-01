package nl.knaw.huygens.alexandria.endpoint.resource;

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.ProcessStatusMap;
import nl.knaw.huygens.alexandria.api.model.text.TextEntity;
import nl.knaw.huygens.alexandria.api.model.text.TextImportStatus;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationList;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewEntity;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.ResourceTextFactory;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.ConflictException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.text.TextPrototype;
import nl.knaw.huygens.alexandria.textgraph.DotFactory;
import nl.knaw.huygens.alexandria.textgraph.TextGraphImportTask;
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil;
import nl.knaw.huygens.alexandria.textgraph.TextRangeAnnotationValidatorFactory;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static java.util.stream.Collectors.toList;

public class ResourceTextEndpoint extends JSONEndpoint {
  private final AlexandriaService service;
  private final UUID resourceUUID;
  private final AlexandriaResource resource;
  private ExecutorService executorService;
  private final LocationBuilder locationBuilder;
  private final ProcessStatusMap<TextImportStatus> taskStatusMap;
  private AlexandriaConfiguration config;
  private ResourceTextFactory textFactory;
  private TextRangeAnnotationValidatorFactory textRangeAnnotationValidator;

  @Inject
  public ResourceTextEndpoint(AlexandriaService service, //
      AlexandriaConfiguration config, //
      ResourceValidatorFactory validatorFactory, //
      ExecutorService executorService, //
      LocationBuilder locationBuilder, //
      ResourceTextFactory resourceTextFactory, //
      ProcessStatusMap<TextImportStatus> taskStatusMap, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    this.service = service;
    this.config = config;
    this.executorService = executorService;
    this.locationBuilder = locationBuilder;
    this.textFactory = resourceTextFactory;
    this.taskStatusMap = taskStatusMap;
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
  public Response getXML(@QueryParam("view") String view) {
    assertResourceHasText();
    StreamingOutput outputstream = TextGraphUtil.xmlOutputStream(service, resourceUUID, view);
    return ok(outputstream);
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
    return ok();
  }

  /* private methods */

  private void startTextProcessing(String xml) {
    TextGraphImportTask task = new TextGraphImportTask(service, locationBuilder, xml, resource);
    taskStatusMap.put(resource.getId(), task.getStatus());
    if (config.asynchronousEndpointsAllowed()) {
      executorService.execute(task);
    } else {
      // For now, for the acceptance tests.
      task.run();
    }
  }

  private void startAnnotating(TextRangeAnnotationList newTextRangeAnnotationList) {
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
