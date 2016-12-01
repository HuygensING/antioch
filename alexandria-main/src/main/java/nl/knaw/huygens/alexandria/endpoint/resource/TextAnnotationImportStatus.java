package nl.knaw.huygens.alexandria.endpoint.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;
import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.ProcessStatus;
import nl.knaw.huygens.alexandria.api.model.text.TextRangeAnnotationInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonTypeName(JsonTypeNames.TEXTANNOTATIONIMPORTSTATUS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextAnnotationImportStatus extends ProcessStatus {
  private List<String> errors = Lists.newArrayList();
  private Map<UUID,TextRangeAnnotationInfo> textRangeAnnotationInfoMap = new HashMap<>();

  public List<String> getErrors() {
    return errors;
  }

  public Map<UUID, TextRangeAnnotationInfo> getTextRangeAnnotationInfoMap() {
    return textRangeAnnotationInfoMap;
  }
}
