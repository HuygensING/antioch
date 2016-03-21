package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.stream.Collectors.toList;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
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

import com.google.common.collect.Maps;

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

public class ResourceTextEndpoint extends JSONEndpoint {
  private final AlexandriaService service;
  private final UUID resourceId;
  private final AlexandriaResource resource;
  private LocationBuilder locationBuilder;
  private static Map<UUID, TextImportTask.Status> taskStatusMap = Maps.newHashMap();
  private ExecutorService executorService;

  @Inject
  public ResourceTextEndpoint(AlexandriaService service, //
      ResourceValidatorFactory validatorFactory, //
      ExecutorService executorService, //
      LocationBuilder locationBuilder, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    this.service = service;
    this.executorService = executorService;
    this.locationBuilder = locationBuilder;
    this.resource = validatorFactory.validateExistingResource(uuidParam).notTentative();
    this.resourceId = resource.getId();
  }

  @GET
  @Produces(MediaType.TEXT_XML)
  @ApiOperation("get text as xml")
  public Response getXMLText() {
    return getTextResponse();
  }

  // @GET
  // @Produces(MediaType.TEXT_PLAIN)
  // @ApiOperation("get text as plain text")
  // public Response getPlainText() {
  // return getTextResponse();
  // }

  @PUT
  @Consumes(MediaType.TEXT_XML)
  @ApiOperation("set text from xml")
  public Response setTextFromXml(@NotNull @Valid String xml) {
    assertResourceHasNoText();
    BaseLayerDefinition bld = service.getBaseLayerDefinitionForResource(resourceId).orElseThrow(noBaseLayerDefined());
    startTextProcessing(xml, bld);
    return Response.accepted()//
        .location(locationBuilder.locationOf(resource, "text", "status"))//
        .build();
  }

  private void startTextProcessing(String xml, BaseLayerDefinition bld) {
    removeExpiredTasks();
    TextImportTask task = new TextImportTask(service, locationBuilder, bld, xml, resource, ThreadContext.getUserName());
    taskStatusMap.put(resource.getId(), task.getStatus());
    // task.run();
    executorService.execute(task);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("set text from text protoype")
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
  public Response getTextImportStatus() {
    removeExpiredTasks();
    if (taskStatusMap.containsKey(resourceId)) {
      TextImportTask.Status textImportTaskStatus = taskStatusMap.get(resourceId);
      return Response.ok().entity(textImportTaskStatus).build();
    }
    throw new NotFoundException();
  }

  private Supplier<ConflictException> noBaseLayerDefined() {
    return () -> new ConflictException(String.format("No base layer defined for resource: %s", resourceId));
  }

  private void assertResourceHasNoText() {
    if (resource.hasText()) {
      throw new ConflictException("This resource already has a text, which cannot be replaced.");
    }
  }

  private Response getTextResponse() {
    return ok(streamOut(resourceTextAsStream()));
  }

  private InputStream resourceTextAsStream() {
    return service.getResourceTextAsStream(resourceId).orElseThrow(() -> new NotFoundException("no text found"));
  }

  private InputStream streamIn(String body) {
    return IOUtils.toInputStream(body);
  }

  private StreamingOutput streamOut(InputStream is) {
    return output -> IOUtils.copy(is, output);
  }

  private void removeExpiredTasks() {
    List<UUID> expiredEntries = taskStatusMap.keySet().stream()//
        .filter(uuid -> taskStatusMap.get(uuid).isExpired())//
        .collect(toList());
    expiredEntries.forEach(key -> taskStatusMap.remove(key));
  }

}
