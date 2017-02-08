package nl.knaw.huygens.alexandria.endpoint.iiif;

import com.google.common.collect.Lists;
import nl.knaw.huygens.alexandria.api.model.ProcessStatus;

import java.util.List;
import java.util.Map;

public class AnnotationListImportStatus extends ProcessStatus {
  private Map<String, Object> processedList;
  private List<String> errors = Lists.newArrayList();

  public void setProcessedList(Map<String, Object> processedList) {
    this.processedList = processedList;
  }

  public Map<String, Object> getProcessedList() {
    return processedList;
  }

  public List<String> getErrors() {
    return errors;
  }

  public boolean hasErrors() {
    return errors.size() > 0;
  }

}
