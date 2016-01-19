package nl.knaw.huygens.alexandria.endpoint.resource;

import java.io.InputStream;
import java.util.Optional;
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

import org.apache.commons.io.IOUtils;

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
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
    return getTextResponse();
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation("get text as plain text")
  public Response getPlainText() {
    return getTextResponse();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("set text from text protoype")
  public Response setTextWithPrototype(@NotNull @Valid TextPrototype prototype) {
    String body = prototype.getBody();
    service.setResourceTextFromStream(resourceUUID, IOUtils.toInputStream(body));
    return Response.ok(body).build();
  }

  @PUT
  @Consumes(MediaType.TEXT_XML)
  @ApiOperation("set text from xml")
  public Response setTextFromXml(@NotNull @Valid String xml) {
    service.setResourceTextFromStream(resourceUUID, IOUtils.toInputStream(xml));
    return Response.ok(xml).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  @ApiOperation("set text from stream")
  public Response setTextFromXml(InputStream inputStream) {
    service.setResourceTextFromStream(resourceUUID, inputStream);
    return Response.ok().build();
  }

  private Response getTextResponse() {
    Optional<InputStream> textStream = service.getResourceTextAsStream(resourceUUID);//
    if (textStream.isPresent()) {
      StreamingOutput stream = output -> {
        IOUtils.copy(textStream.get(), output);
      };
      return Response.ok(stream).build();
    }
    return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity("no text found").build();
  }
  // private Response getTextResponse() {
  // Optional<String> text = service.getResourceText(resourceUUID);//
  // if (text.isPresent()) {
  // return Response.ok(text.get()).build();
  // }
  // return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity("no text found").build();
  // }

  // // TODO: why does this throw a java.lang.IllegalStateException: Illegal attempt to call getOutputStream() after getWriter() has
  // // already been called. ?
  // private Response getText() {
  // String text = service.getResourceText(resourceUUID)//
  // .orElseThrow(() -> new NotFoundException("no text found"));
  // return Response.ok(text).build();
  // }
}
