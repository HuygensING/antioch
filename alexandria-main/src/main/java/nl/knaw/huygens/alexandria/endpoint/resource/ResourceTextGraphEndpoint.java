package nl.knaw.huygens.alexandria.endpoint.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.api.model.BaseLayerDefinition;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.ConflictException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.jaxrs.ThreadContext;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.text.TextPrototype;
import nl.knaw.huygens.alexandria.textgraph.DotFactory;
import nl.knaw.huygens.alexandria.textgraph.TextGraphImportStatus;
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

  @Inject
  public ResourceTextGraphEndpoint(AlexandriaService service, //
      ResourceValidatorFactory validatorFactory, //
      ExecutorService executorService, //
      LocationBuilder locationBuilder, //
      TextGraphTaskStatusMap taskStatusMap, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    this.service = service;
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
        .location(locationBuilder.locationOf(resource, "text", "status"))//
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
    TextEntity text = TextEntity.of(resourceId).withLocationBuilder(locationBuilder);
    return Response.ok().entity(text).build();
  }

  @GET
  @Path("status")
  public Response getTextGraphImportStatus() {
    TextGraphImportStatus textGraphImportTaskStatus = taskStatusMap.get(resourceId)//
        .orElseThrow(() -> new NotFoundException());
    return Response.ok().entity(textGraphImportTaskStatus).build();
  }

  @GET
  @Path("baselayer")
  @Produces(MediaType.TEXT_XML)
  @ApiOperation("get baselayer as xml")
  public Response getBaseLayerXML() {
    assertResourceHasText();
    BaseLayerDefinition baseLayerDefinition = service.getBaseLayerDefinitionForResource(resourceId)//
        .orElseThrow(noBaseLayerDefined());

    StreamingOutput outputstream = TextGraphUtil.streamBaseLayerXML(service, resourceId, baseLayerDefinition);
    return ok(outputstream);
  }

  @GET
  @Path("xml")
  @Produces(MediaType.TEXT_XML)
  @ApiOperation("get textgraph as xml")
  public Response getXML() {
    assertResourceHasText();
    StreamingOutput outputstream = TextGraphUtil.streamXML(service, resourceId);
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
    TextGraphImportTask task = new TextGraphImportTask(service, locationBuilder, xml, resource, ThreadContext.getUserName());
    taskStatusMap.put(resource.getId(), task.getStatus());
    executorService.execute(task);
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

  private void assertResourceHasBaseLayerDefinition() {
    service.getBaseLayerDefinitionForResource(resourceId)//
        .orElseThrow(noBaseLayerDefined());
  }

  private Supplier<ConflictException> noBaseLayerDefined() {
    return () -> new ConflictException(String.format("No base layer defined for resource: %s", resourceId));
  }
}
