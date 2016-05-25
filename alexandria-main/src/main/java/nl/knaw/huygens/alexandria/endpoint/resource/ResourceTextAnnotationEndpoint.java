package nl.knaw.huygens.alexandria.endpoint.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
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

import org.apache.commons.lang3.StringUtils;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.Position;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationInfo;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil;
import nl.knaw.huygens.alexandria.util.XMLUtil;
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
      @NotNull TextRangeAnnotation textAnnotation//
  ) {
    String xml = getXML();
    String annotated = validateTextAnnotation(textAnnotation, xml);
    URI location = locationBuilder.locationOf(resource, EndpointPaths.TEXT, EndpointPaths.ANNOTATIONS, uuidParam.getValue().toString());
    TextRangeAnnotationInfo info = new TextRangeAnnotationInfo().setAnnotates(annotated);
    return Response.created(location).entity(info).build();
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

  private String validateTextAnnotation(TextRangeAnnotation textAnnotation, String xml) {
    String annotated = validatePosition(textAnnotation.getPosition(), xml);
    validateName(textAnnotation.getName());
    validateAnnotator(textAnnotation.getAnnotator());
    return annotated;
  }

  private void validateAnnotator(String annotator) {
    if (StringUtils.isEmpty(annotator)) {
      throw new BadRequestException("No annotator specified.");
    }
    Optional<String> validAnnotator = service.readResourceAnnotators(resourceId).stream()//
        .map(Annotator::getCode)//
        .filter(annotator::equals)//
        .findFirst();
    if (!validAnnotator.isPresent()) {
      throw new BadRequestException("Resource has no annotator with code '" + annotator + "'.");
    }
  }

  private String validatePosition(Position position, String xml) {
    QueryableDocument qDocument = QueryableDocument.createFromXml(xml, true);
    validate(qDocument, //
        "count(//*[@xml:id='" + position.getXmlId() + "'])", //
        1d, //
        "The text does not contain an element with the specified xml:id."//
    );
    String xpath = "substring(//*[@xml:id='" + position.getXmlId() + "']," + position.getOffset() + "," + position.getLength() + ")";
    validate(qDocument, //
        "string-length(" + xpath + ")", //
        new Double(position.getLength()), //
        "The specified offset/length is illegal."//
    );
    try {
      return qDocument.evaluateXPathToString(xpath);
    } catch (XPathExpressionException e) {
      e.printStackTrace();
      return "";
    }
  }

  private void validate(QueryableDocument qDocument, String xpath, Double expectation, String errorMessage) {
    Log.info("xpath = '{}'", xpath);
    try {
      Double evaluation = qDocument.evaluateXPathToDouble(xpath);
      Log.info("evaluation = {}", evaluation);
      if (!evaluation.equals(expectation)) {
        throw new BadRequestException(errorMessage);
      }

    } catch (XPathExpressionException e) {
      e.printStackTrace();
      throw new BadRequestException(errorMessage);
    }
  }

  private void validateName(String elementName) {
    List<String> validationErrors = XMLUtil.validateElementName(elementName);
    if (!validationErrors.isEmpty()) {
      throw new BadRequestException("The specified annotation name is illegal.");
    }

  }

}
