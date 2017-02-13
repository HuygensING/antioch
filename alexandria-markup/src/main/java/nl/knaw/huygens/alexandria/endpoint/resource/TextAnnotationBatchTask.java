package nl.knaw.huygens.alexandria.endpoint.resource;

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

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.StreamingOutput;

import com.google.common.base.Joiner;

import nl.knaw.huygens.alexandria.api.model.text.TextAnnotationImportStatus;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationInfo;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationList;
import nl.knaw.huygens.alexandria.exception.ConflictException;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.textgraph.TextGraphUtil;
import nl.knaw.huygens.alexandria.textgraph.TextRangeAnnotationValidatorFactory;

public class TextAnnotationBatchTask implements Runnable {

  private AlexandriaService service;
  private TextAnnotationImportStatus status;
  private UUID resourceUUID;
  private TextRangeAnnotationList newTextRangeAnnotationList;
  private TextRangeAnnotationValidatorFactory textRangeAnnotationValidator;

  public TextAnnotationBatchTask(AlexandriaService service, TextRangeAnnotationList newTextRangeAnnotationList, TextRangeAnnotationValidatorFactory textRangeAnnotationValidator,
      AlexandriaResource resource) {
    this.service = service;
    this.resourceUUID = resource.getId();
    this.newTextRangeAnnotationList = newTextRangeAnnotationList;
    this.textRangeAnnotationValidator = textRangeAnnotationValidator;
    this.status = new TextAnnotationImportStatus();
  }

  public TextAnnotationImportStatus getStatus() {
    return status;
  }

  @Override
  public void run() {
    status.setStarted();
    try {
      service.runInTransaction(() -> {
        String xml = getXML(); // only used to determine the annotated text, so no need to refresh after every annotation
        newTextRangeAnnotationList.forEach(newTextRangeAnnotation -> {
          String annotated = TextRangeAnnotationValidatorFactory.getAnnotatedText(newTextRangeAnnotation.getPosition(), xml);
          textRangeAnnotationValidator.calculateAbsolutePosition(newTextRangeAnnotation, annotated);

          UUID annotationUUID = newTextRangeAnnotation.getId();
          Optional<TextRangeAnnotation> existingTextRangeAnnotation = service.readTextRangeAnnotation(resourceUUID, annotationUUID);
          if (existingTextRangeAnnotation.isPresent()) {
            TextRangeAnnotation oldTextRangeAnnotation = existingTextRangeAnnotation.get();
            textRangeAnnotationValidator.validate(newTextRangeAnnotation, oldTextRangeAnnotation);
            service.deprecateTextRangeAnnotation(annotationUUID, newTextRangeAnnotation);

          } else {
            textRangeAnnotationValidator.validate(newTextRangeAnnotation);
            service.setTextRangeAnnotation(resourceUUID, newTextRangeAnnotation);
          }
          status.getTextRangeAnnotationInfoMap().put(newTextRangeAnnotation.getId(), new TextRangeAnnotationInfo().setAnnotates(annotated));
        });
      });

    } catch (Exception e) {
      e.printStackTrace();
      status.getErrors().add("Exception thrown: " + e);
      status.getErrors().add("Exception message: " + e.getMessage());
      status.getErrors().add("Stacktrace: " + Joiner.on("\n").join(e.getStackTrace()));
      if (e instanceof ConflictException) {
        ConflictException conflictException = (ConflictException) e;
        int httpStatus = conflictException.getResponse().getStatus();
        status.setBreakingErrorMessage(httpStatus + " " + conflictException.getErrorMessage());
      } else {
        status.setBreakingErrorMessage(e.getMessage());
      }
    }
    status.setDone();

  }

  private String getXML() {
    StreamingOutput outputStream = TextGraphUtil.streamXML(service, resourceUUID);
    return TextGraphUtil.asString(outputStream);
  }

}
