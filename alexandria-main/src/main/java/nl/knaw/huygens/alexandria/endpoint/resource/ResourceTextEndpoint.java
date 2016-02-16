package nl.knaw.huygens.alexandria.endpoint.resource;

import java.io.IOException;

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

import java.io.InputStream;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.exception.ConflictException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.BaseLayerDefinition;
import nl.knaw.huygens.alexandria.model.BaseLayerDefinition.BaseElementDefinition;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.text.TextUtil;

public class ResourceTextEndpoint extends JSONEndpoint {
  private static final BaseLayerDefinition DEFAULT_BASELAYER_DEFINITION = BaseLayerDefinition.withBaseElements(BaseElementDefinition.withName("text"),
      BaseElementDefinition.withName("p").withAttributes("xml:id"));
  private final AlexandriaService service;
  private final UUID resourceId;
  private final AlexandriaResource resource;
  private LocationBuilder locationBuilder;

  @Inject
  public ResourceTextEndpoint(AlexandriaService service, //
      ResourceValidatorFactory validatorFactory, //
      LocationBuilder locationBuilder, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    this.service = service;
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

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation("get text as plain text")
  public Response getPlainText() {
    return getTextResponse();
  }

  @PUT
  @Consumes(MediaType.TEXT_XML)
  @ApiOperation("set text from xml")
  public Response setTextFromXml(@NotNull @Valid String xml) {
    verifyResourceHasNoText();
    Pair<BaseLayerDefinition, UUID> pair = service.getBaseLayerDefinitionForResource(resourceId)//
        // .orElse(Pair.of(DEFAULT_BASELAYER_DEFINITION, resourceId));
        .orElseThrow(() -> new ConflictException("No base layer defined for this resource."));
    String baseLayer = TextUtil.extractBaseLayer(xml, pair.getLeft());
    service.setResourceTextFromStream(resourceId, streamIn(baseLayer));
    ResourceTextUploadEntity resourceTextUploadEntity = ResourceTextUploadEntity.of(pair.getRight()).withLocationBuilder(locationBuilder);
    return ok(resourceTextUploadEntity);
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

  private void verifyResourceHasNoText() {
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
}
