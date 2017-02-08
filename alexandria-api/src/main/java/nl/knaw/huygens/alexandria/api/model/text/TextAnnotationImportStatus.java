package nl.knaw.huygens.alexandria.api.model.text;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.ProcessStatus;

@JsonTypeName(JsonTypeNames.TEXTANNOTATIONIMPORTSTATUS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextAnnotationImportStatus extends ProcessStatus {
  private List<String> errors = Lists.newArrayList();
  private Map<UUID, TextRangeAnnotationInfo> textRangeAnnotationInfoMap = new ConcurrentHashMap<>();
  private String breakingErrorMessage;

  public List<String> getErrors() {
    return errors;
  }

  public void setBreakingErrorMessage(String breakingErrorMessage) {
    this.breakingErrorMessage = breakingErrorMessage;
  }

  public boolean hasErrors() {
    return errors.size() > 0;
  }

  public String getBreakingErrorMessage() {
    return breakingErrorMessage;
  }

  public Map<UUID, TextRangeAnnotationInfo> getTextRangeAnnotationInfoMap() {
    return textRangeAnnotationInfoMap;
  }

}
