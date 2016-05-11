package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.TextImportStatus;
import nl.knaw.huygens.alexandria.api.model.TextView;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.ConflictException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.text.TextPrototype;
import nl.knaw.huygens.alexandria.textgraph.DotFactory;
import nl.knaw.huygens.alexandria.textgraph.TextGraphImportTask;
import nl.knaw.huygens.alexandria.textgraph.TextGraphTaskStatusMap;
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil;

public class ResourceTextGraphEndpoint extends JSONEndpoint {
  private final AlexandriaService service;
  private final UUID resourceId;
  private final AlexandriaResource resource;
  private ExecutorService executorService;
  private final LocationBuilder locationBuilder;
  private final TextGraphTaskStatusMap taskStatusMap;
  private AlexandriaConfiguration config;

  @Inject
  public ResourceTextGraphEndpoint(AlexandriaService service, //
      AlexandriaConfiguration config,//
      ResourceValidatorFactory validatorFactory, //
      ExecutorService executorService, //
      LocationBuilder locationBuilder, //
      TextGraphTaskStatusMap taskStatusMap, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    this.service = service;
    this.config = config;
    this.executorService = executorService;
    this.locationBuilder = locationBuilder;
    this.taskStatusMap = taskStatusMap;
    this.resource = validatorFactory.validateExistingResource(uuidParam).notTentative();
    this.resourceId = resource.getId();
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
    List<TextViewEntity> textViewEntities = service.getTextViewsForResource(resourceId)//
        .stream()//
        .map(tv -> new TextViewEntity(resourceId, tv, locationBuilder))//
        .collect(toList());
    TextEntity text = new TextEntity(resourceId, locationBuilder, textViewEntities);
    return ok(text);
  }

  @Path(EndpointPaths.TEXTVIEWS)
  public Class<ResourceTextViewEndpoint> getTextViewEndpoint() {
    return ResourceTextViewEndpoint.class; // no instantiation of our own; let Jersey handle the lifecycle
  }

  @GET
  @Path("status")
  public Response getTextGraphImportStatus() {
    TextImportStatus textGraphImportTaskStatus = taskStatusMap.get(resourceId)//
        .orElseThrow(() -> new NotFoundException());
    return ok(textGraphImportTaskStatus);
  }

  @GET
  @Path("xml")
  @Produces(MediaType.TEXT_XML)
  @ApiOperation("get textgraph as xml")
  public Response getXML(@QueryParam("view") String view) {
    assertResourceHasText();
    StreamingOutput outputstream;
    if (StringUtils.isNotBlank(view)) {
      TextView textView = service.getTextView(resourceId, view)//
          .orElseThrow(() -> new NotFoundException("No view '" + view + "' found for this resource."));
      outputstream = TextGraphUtil.streamTextViewXML(service, resourceId, textView);

    } else {
      outputstream = TextGraphUtil.streamXML(service, resourceId);
    }

    return ok(outputstream);
  }

  @GET
  @Path("dot")
  @Produces(MediaType.TEXT_PLAIN + "; charSet=utf-8")
  @ApiOperation("get textgraph as .dot output")
  public Response getDot() {
    assertResourceHasText();
    String dot = DotFactory.createDot(service, resourceId);
    return ok(dot);
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
