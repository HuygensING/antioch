package nl.knaw.huygens.alexandria.textgraph;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.BadRequestException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;

import nl.knaw.huygens.alexandria.api.model.Annotator;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.AbsolutePosition;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation.Position;
import nl.knaw.huygens.alexandria.exception.ConflictException;
import nl.knaw.huygens.alexandria.service.MarkupService;
import nl.knaw.huygens.alexandria.util.XMLUtil;
import nl.knaw.huygens.tei.QueryableDocument;

public class TextRangeAnnotationValidatorFactory {

  private MarkupService service;
  private UUID resourceUUID;

  public TextRangeAnnotationValidatorFactory(MarkupService service, UUID resourceUUID) {
    this.service = service;
    this.resourceUUID = resourceUUID;
  }

  public void calculateAbsolutePosition(TextRangeAnnotation newTextRangeAnnotation, String annotatedText) {
    Position relativePosition = newTextRangeAnnotation.getPosition();
    AbsolutePosition absolutePosition = new AbsolutePosition();
    if (relativePosition.getXmlId().isPresent()) {
      absolutePosition.setXmlId(relativePosition.getXmlId().get())//
          .setOffset(relativePosition.getOffset().orElse(1))//
          .setLength(relativePosition.getLength().orElse(annotatedText.length()));
    } else {
      // Log.info("relpos={}", relativePosition);
      UUID targetAnnotationUUID = relativePosition.getTargetAnnotationId()//
          .orElseThrow(() -> new BadRequestException("Position has neither xmlId nor targetAnnotationId, it needs one or the other."));
      TextRangeAnnotation targetTextRangeAnnotation = service.readTextRangeAnnotation(resourceUUID, targetAnnotationUUID)//
          .orElseThrow(() -> new BadRequestException("targetAnnotationId " + targetAnnotationUUID + " does not refer to an existing annotation on resource " + resourceUUID + "."));
      AbsolutePosition targetAbsolutePosition = targetTextRangeAnnotation.getAbsolutePosition();
      absolutePosition.setXmlId(targetAbsolutePosition.getXmlId())//
          .setOffset(targetAbsolutePosition.getOffset())//
          .setLength(targetAbsolutePosition.getLength())//
      ;
      newTextRangeAnnotation.setUseOffset(targetTextRangeAnnotation.getUseOffset());

    }
    // Log.info("absolutePosition={}", absolutePosition);
    newTextRangeAnnotation.setAbsolutePosition(absolutePosition);
  }

  public void validate(TextRangeAnnotation textRangeAnnotation) {
    validateName(textRangeAnnotation.getName());
    validateAnnotator(textRangeAnnotation.getAnnotator());
    validateAttributes(textRangeAnnotation.getAttributes());
    if (service.nonNestingOverlapWithExistingTextRangeAnnotationForResource(textRangeAnnotation, resourceUUID)) {
      throw new ConflictException("Overlapping annotations with the same name and responsibility.");
    }
  }

  public void validate(TextRangeAnnotation newTextRangeAnnotation, TextRangeAnnotation existingTextRangeAnnotation) {
    validateNewAttributes(newTextRangeAnnotation, existingTextRangeAnnotation);
    validateNewName(newTextRangeAnnotation, existingTextRangeAnnotation);
    validateNewPosition(newTextRangeAnnotation, existingTextRangeAnnotation);
    validateNewAnnotator(newTextRangeAnnotation, existingTextRangeAnnotation);
    validate(newTextRangeAnnotation);
  }

  public static String getAnnotatedText(Position position, String xml) {
    QueryableDocument qDocument = getQueryableDocument(xml);
    AtomicReference<String> annotated = new AtomicReference<>("");
    position.getXmlId().ifPresent(xmlId -> {
      validate(qDocument, //
          "count(//*[@xml:id='" + xmlId + "'])", //
          1d, //
          "The text does not contain an element with the specified xml:id."//
      );
      String xpath = MessageFormat.format("string(//*[@xml:id=''{0}''])", xmlId);
      if (position.getOffset().isPresent()) {
        xpath = "substring(//*[@xml:id='" + xmlId + "']," + position.getOffset().get() + "," + position.getLength().orElse(-1) + ")";
        validate(qDocument, //
            "string-length(" + xpath + ")", //
            new Double(position.getLength().get()), //
            "The specified offset/length is illegal."//
        );
      }

      try {
        // Log.debug("xpath = {}", xpath);
        annotated.set(qDocument.evaluateXPathToString(xpath));
      } catch (XPathExpressionException e) {
        e.printStackTrace();
      }

    });
    return annotated.get().replace(QueryableDocumentCache.AMPERSAND_PLACEHOLDER, "&");
  }

  private static QueryableDocument getQueryableDocument(String xml) {
    // String processedXml = xml.replace("&", QueryableDocumentCache.AMPERSAND_PLACEHOLDER);
    // return QueryableDocument.createFromXml(processedXml, true);

    return QueryableDocumentCache.get(xml);
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
      throw new BadRequestException("You're not allowed to change the annotator '" + existingAnnotator + "'");
    }
  }

  private void validateNewName(TextRangeAnnotation newTextRangeAnnotation, TextRangeAnnotation existingTextRangeAnnotation) {
    String existingName = existingTextRangeAnnotation.getName();
    String newName = newTextRangeAnnotation.getName();
    if (!newName.equals(existingName)) {
      throw new BadRequestException("You're not allowed to change the annotation name '" + existingName + "'");
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
    Optional<String> validAnnotator = service.readResourceAnnotators(resourceUUID).stream()//
        .map(Annotator::getCode)//
        .filter(annotator::equals)//
        .findAny();
    if (!validAnnotator.isPresent()) {
      throw new BadRequestException("Resource has no annotator with code '" + annotator + "'.");
    }
  }

  private static void validate(QueryableDocument qDocument, String xpath, Double expectation, String errorMessage) {
    // Log.info("xpath = '{}'", xpath);
    try {
      Double evaluation = qDocument.evaluateXPathToDouble(xpath);
      // Log.info("evaluation = {}", evaluation);
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
