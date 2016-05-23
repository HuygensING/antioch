package nl.knaw.huygens.alexandria.endpoint.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.xpath.XPathExpressionException;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.text.ResourceTextAnnotation;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil;
import nl.knaw.huygens.tei.QueryableDocument;

public class ResourceTextAnnotationEndpoint extends JSONEndpoint {

  private LocationBuilder locationBuilder;
  private AlexandriaService service;
  private AlexandriaResource resource;
  private UUID resourceId;

  @Inject
  public ResourceTextAnnotationEndpoint(AlexandriaService service, //
      ResourceValidatorFactory validatorFactory, //
      LocationBuilder locationBuilder, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    this.resource = validatorFactory.validateExistingResource(uuidParam).notTentative().hasText().get();
    this.resourceId = resource.getId();
  }

  @PUT
  @Path("{uuid}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response setAnnotation(//
      @PathParam("uuid") final UUIDParam uuidParam, //
      @NotNull ResourceTextAnnotation textAnnotation//
  ) {
    String xml = getXML();
    validateTextAnnotation(textAnnotation, xml);
    URI location = locationBuilder.locationOf(resource, EndpointPaths.TEXT, EndpointPaths.ANNOTATIONS, uuidParam.getValue().toString());
    return created(location);
  }

  private String getXML() {
    StreamingOutput outputstream = TextGraphUtil.streamXML(service, resourceId);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    String xml = null;
    try {
      outputstream.write(output);
      output.close();
      xml = output.toString();
    } catch (WebApplicationException | IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return xml;
  }

  private void validateTextAnnotation(ResourceTextAnnotation textAnnotation, String xml) {
    QueryableDocument qDocument = QueryableDocument.createFromXml(xml, true);
    validate(qDocument, //
        "count(//*[@xml:id='" + textAnnotation.getPosition().getXmlId() + "'])", //
        "The text does not contain an element with the specified xml:id."//
    );
  }

  private void validate(QueryableDocument qDocument, String xpath, String message) {
    Log.info("xpath = '{}'", xpath);
    try {
      Double evaluation = qDocument.evaluateXPathToDouble(xpath);
      if (evaluation != 1d) {
        throw new BadRequestException(message);
      }
      Log.info("evaluation = {}", evaluation);

    } catch (XPathExpressionException e) {
      e.printStackTrace();
      throw new BadRequestException(message);
    }
  }
}
