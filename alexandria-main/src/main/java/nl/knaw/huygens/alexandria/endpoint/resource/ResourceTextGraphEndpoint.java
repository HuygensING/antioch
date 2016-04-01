package nl.knaw.huygens.alexandria.endpoint.resource;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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

import static java.util.stream.Collectors.toSet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
import org.jooq.lambda.Unchecked;

import com.google.common.collect.Maps;

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.api.model.BaseElementDefinition;
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
import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;
import nl.knaw.huygens.alexandria.textgraph.TextGraphImportStatus;
import nl.knaw.huygens.alexandria.textgraph.TextGraphImportTask;
import nl.knaw.huygens.alexandria.textgraph.TextGraphSegment;
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
    if (!resource.hasText()) {
      throw new NotFoundException("this resource has no text");
    }
    BaseLayerDefinition baseLayerDefinition = service.getBaseLayerDefinitionForResource(resourceId)//
        .orElseThrow(noBaseLayerDefined());

    Stream<TextGraphSegment> textGraphSegmentStream = service.getTextGraphSegmentStream(resourceId);
    List<BaseElementDefinition> baseElementDefinitions = baseLayerDefinition.getBaseElementDefinitions();
    StreamingOutput outputstream = output -> {
      Writer writer = new BufferedWriter(new OutputStreamWriter(output));
      textGraphSegmentStream.forEach(Unchecked.consumer(//
          segment -> streamTextGraphSegment(writer, segment, baseElementDefinitions)//
      ));
      writer.flush();
    };

    return ok(outputstream);
  }

  private void streamTextGraphSegment(Writer writer, TextGraphSegment segment, List<BaseElementDefinition> baseElementDefinitions) throws IOException {
    Set<String> baseElementNames = baseElementDefinitions.stream().map(BaseElementDefinition::getName).collect(toSet());
    Map<String, List<String>> baseElementAttributes = Maps.newHashMap();
    for (BaseElementDefinition bed : baseElementDefinitions) {
      baseElementAttributes.put(bed.getName(), bed.getBaseAttributes());
    }
    if (segment.isMilestone()) {
      TextAnnotation milestone = segment.getMilestone();
      String name = milestone.getName();
      if (baseElementNames.contains(name)) {
        Map<String, String> baseAttributes = baseAttributes(baseElementAttributes.get(name), milestone);
        String openTag = TextGraphUtil.getOpenTag(name, baseAttributes);
        writer.write(openTag);
      }

    } else {
      for (TextAnnotation textAnnotation : segment.getTextAnnotationsToClose()) {
        String name = textAnnotation.getName();
        if (baseElementNames.contains(name)) {
          String closeTag = TextGraphUtil.getCloseTag(name);
          writer.write(closeTag);
        }
      }
      writer.write(segment.getTextSegment());
      for (TextAnnotation textAnnotation : segment.getTextAnnotationsToOpen()) {
        String name = textAnnotation.getName();
        if (baseElementNames.contains(name)) {
          Map<String, String> baseAttributes = baseAttributes(baseElementAttributes.get(name), textAnnotation);
          String openTag = TextGraphUtil.getOpenTag(name, baseAttributes);
          writer.write(openTag);
        }
      }
    }
  }

  private Map<String, String> baseAttributes(List<String> baseElementAttributeNames, TextAnnotation milestone) {
    Map<String, String> allAttributes = milestone.getAttributes();
    Map<String, String> baseAttributes = Maps.newHashMap();
    for (String name : baseElementAttributeNames) {
      if (allAttributes.containsKey(name)) {
        baseAttributes.put(name, allAttributes.get(name));
      }
    }
    return baseAttributes;
  }

  private void startTextProcessing(String xml) {
    TextGraphImportTask task = new TextGraphImportTask(service, locationBuilder, xml, resource, ThreadContext.getUserName());
    taskStatusMap.put(resource.getId(), task.getStatus());
    executorService.execute(task);
  }

  private Supplier<ConflictException> noBaseLayerDefined() {
    return () -> new ConflictException(String.format("No base layer defined for resource: %s", resourceId));
  }

  private void assertResourceHasNoText() {
    if (resource.hasText()) {
      throw new ConflictException("This resource already has a text, which cannot be replaced.");
    }
  }

  private InputStream resourceTextAsStream() {
    return service.getResourceTextAsStream(resourceId).orElseThrow(() -> new NotFoundException("no text found"));
  }

  private StreamingOutput streamOut(InputStream is) {
    return output -> IOUtils.copy(is, output);
  }

}
