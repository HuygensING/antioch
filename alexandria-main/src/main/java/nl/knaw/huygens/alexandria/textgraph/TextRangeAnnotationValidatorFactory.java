package nl.knaw.huygens.alexandria.textgraph;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.BadRequestException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.Position;
import nl.knaw.huygens.alexandria.exception.ConflictException;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.util.XMLUtil;
import nl.knaw.huygens.tei.QueryableDocument;

public class TextRangeAnnotationValidatorFactory {

  private AlexandriaService service;
  private UUID resourceUUID;

  public TextRangeAnnotationValidatorFactory(AlexandriaService service, UUID resourceUUID) {
    this.service = service;
    this.resourceUUID = resourceUUID;
  }

  public String validate(TextRangeAnnotation textRangeAnnotation, String xml) {
    String annotated = validatePosition(textRangeAnnotation.getPosition(), xml);
    boolean hasNoOffset = !textRangeAnnotation.hasOffset();
    if (hasNoOffset) {
      textRangeAnnotation.getPosition().setOffset(1).setLength(annotated.length());
    }
    validateName(textRangeAnnotation.getName());
    validateAnnotator(textRangeAnnotation.getAnnotator());
    validateAttributes(textRangeAnnotation.getAttributes());
    if (service.overlapsWithExistingTextRangeAnnotationForResource(textRangeAnnotation, resourceUUID)) {
      throw new ConflictException("Overlapping annotations with the same name and responsibility.");
    }
    return annotated;
  }

  public String validate(TextRangeAnnotation newTextRangeAnnotation, TextRangeAnnotation existingTextRangeAnnotation, String xml) {
    validateNewAttributes(newTextRangeAnnotation, existingTextRangeAnnotation);
    validateNewName(newTextRangeAnnotation, existingTextRangeAnnotation);
    validateNewPosition(newTextRangeAnnotation, existingTextRangeAnnotation);
    validateNewAnnotator(newTextRangeAnnotation, existingTextRangeAnnotation);
    return validate(newTextRangeAnnotation, xml);
  }

  private void validateNewPosition(TextRangeAnnotation newTextRangeAnnotation, TextRangeAnnotation existingTextRangeAnnotation) {
    Position existingPosition = existingTextRangeAnnotation.getPosition();
    Position newPosition = newTextRangeAnnotation.getPosition();
    boolean positionsAreEquivalent = newPosition.getXmlId().equals(existingPosition.getXmlId());
    if (newPosition.getOffset().isPresent()) {
      positionsAreEquivalent = positionsAreEquivalent && newPosition.getOffset().get().equals(existingPosition.getOffset().get());
    }
    if (newPosition.getLength().isPresent()) {
      positionsAreEquivalent = positionsAreEquivalent && newPosition.getLength().get().equals(existingPosition.getLength().get());
    }
    if (!positionsAreEquivalent) {
      throw new BadRequestException("You're not allowed to change the annotation position");
    }
  }

  private void validateNewAnnotator(TextRangeAnnotation newTextRangeAnnotation, TextRangeAnnotation existingTextRangeAnnotation) {
    String existingAnnotator = existingTextRangeAnnotation.getAnnotator();
    String newAnnotator = newTextRangeAnnotation.getAnnotator();
    if (!newAnnotator.equals(existingAnnotator)) {
      throw new BadRequestException("You're not allowed to change the annotator");
    }
  }

  private void validateNewName(TextRangeAnnotation newTextRangeAnnotation, TextRangeAnnotation existingTextRangeAnnotation) {
    String existingName = existingTextRangeAnnotation.getName();
    String newName = newTextRangeAnnotation.getName();
    if (!newName.equals(existingName)) {
      throw new BadRequestException("You're not allowed to change the annotation name");
    }
  }

  private void validateNewAttributes(TextRangeAnnotation newTextRangeAnnotation, TextRangeAnnotation existingTextRangeAnnotation) {
    Set<String> existingAttributeNames = existingTextRangeAnnotation.getAttributes().keySet();
    Set<String> newAttributeNames = newTextRangeAnnotation.getAttributes().keySet();
    if (!newAttributeNames.equals(existingAttributeNames)) {
      throw new BadRequestException("You're only allowed to change existing attributes " + existingAttributeNames);
    }
  }

  private void validateAnnotator(String annotator) {
    if (StringUtils.isEmpty(annotator)) {
      throw new BadRequestException("No annotator specified.");
    }
    Optional<String> validAnnotator = service.readResourceAnnotators(resourceUUID).parallelStream()//
        .map(Annotator::getCode)//
        .filter(annotator::equals)//
        .findAny();
    if (!validAnnotator.isPresent()) {
      throw new BadRequestException("Resource has no annotator with code '" + annotator + "'.");
    }
  }

  private static String AMPERSAND_PLACEHOLDER = "☢";

  static String validatePosition(Position position, String xml) {
    String processedXml = xml.replace("&", AMPERSAND_PLACEHOLDER);
    QueryableDocument qDocument = QueryableDocument.createFromXml(processedXml, true);
    validate(qDocument, //
        "count(//*[@xml:id='" + position.getXmlId() + "'])", //
        1d, //
        "The text does not contain an element with the specified xml:id."//
    );
    String xpath = MessageFormat.format("string(//*[@xml:id=''{0}''])", position.getXmlId());
    if (position.getOffset().isPresent()) {
      xpath = "substring(//*[@xml:id='" + position.getXmlId() + "']," + position.getOffset().get() + "," + position.getLength().orElse(-1) + ")";
      validate(qDocument, //
          "string-length(" + xpath + ")", //
          new Double(position.getLength().get()), //
          "The specified offset/length is illegal."//
      );
    }

    String annotated = "";
    try {
      Log.debug("xpath = {}", xpath);
      annotated = qDocument.evaluateXPathToString(xpath);
    } catch (XPathExpressionException e) {
      e.printStackTrace();
    }

    return annotated.replace(AMPERSAND_PLACEHOLDER, "&");
  }

  private static void validate(QueryableDocument qDocument, String xpath, Double expectation, String errorMessage) {
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

  private void validateAttributes(Map<String, String> attributes) {
    attributes.keySet().forEach(this::validateName);
  }

  private void validateName(String elementName) {
    List<String> validationErrors = XMLUtil.validateElementName(elementName);
    if (!validationErrors.isEmpty()) {
      throw new BadRequestException("The specified annotation name is illegal.");
    }

  }

}
