package nl.knaw.huygens.alexandria.endpoint.resource.text;

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

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.endpoint.resource.ResourcesEndpoint;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class ResourceTextEndpoint extends JSONEndpoint {
  private AlexandriaService service;
  private AlexandriaResource resource;
  private UUID resourceUUID;

  @Inject
  public ResourceTextEndpoint(AlexandriaService service, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    this.service = service;
    resourceUUID = uuidParam.getValue();
    resource = service.readResource(resourceUUID)//
        .orElseThrow(ResourcesEndpoint.resourceNotFoundForId(resourceUUID));
    if (resource.isTentative()) {
      throw ResourcesEndpoint.resourceIsTentativeException(resourceUUID);
    }
  }

  @GET
  @Produces(MediaType.TEXT_XML)
  @ApiOperation("get text as xml")
  public Response getXMLText() {
    return getText();
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation("get text as plain text")
  public Response getPlainText() {
    return getText();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("set text from text protoype")
  public Response setTextWithPrototype(@NotNull @Valid TextPrototype prototype) {
    String body = prototype.getBody();
    service.setResourceText(resourceUUID, body);
    return Response.ok(body).build();
  }

  @PUT
  @Consumes(MediaType.TEXT_XML)
  @ApiOperation("set text from xml")
  public Response setTextFromXml(@NotNull @Valid String xml) {
    service.setResourceText(resourceUUID, xml);
    return Response.ok(xml).build();
  }

  @POST
  @Path("query")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("do a text query")
  public Response doTextQuery(@NotNull @Valid TextQuery textQuery) {
    textQuery.setResourceUUID(resourceUUID);
    TextQueryResult result = service.queryText(textQuery);
    if (result.hasErrors()) {
      return Response.status(Status.BAD_REQUEST).entity(result.getErrors()).build();
    }
    return Response.ok(result).build();
  }

  private Response getText() {
    Optional<String> text = service.getResourceText(resourceUUID);//
    if (text.isPresent()) {
      return Response.ok(text.get()).build();
    }
    return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity("no text found").build();
  }

  // // TODO: why does this throw a java.lang.IllegalStateException: Illegal attempt to call getOutputStream() after getWriter() has
  // // already been called. ?
  // private Response getText() {
  // String text = service.getResourceText(resourceUUID)//
  // .orElseThrow(() -> new NotFoundException("no text found"));
  // return Response.ok(text).build();
  // }
}
