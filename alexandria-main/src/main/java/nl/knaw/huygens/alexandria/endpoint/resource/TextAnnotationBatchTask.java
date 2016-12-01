package nl.knaw.huygens.alexandria.endpoint.resource;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.StreamingOutput;

import com.google.common.base.Joiner;

import nl.knaw.huygens.alexandria.api.model.text.TextAnnotationImportStatus;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotation;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationInfo;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationList;
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
        newTextRangeAnnotationList.forEach(newTextRangeAnnotation -> {
          String xml = getXML();
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
    }
    status.setDone();

  }

  private String getXML() {
    StreamingOutput outputStream = TextGraphUtil.streamXML(service, resourceUUID);
    return TextGraphUtil.asString(outputStream);
  }

}
