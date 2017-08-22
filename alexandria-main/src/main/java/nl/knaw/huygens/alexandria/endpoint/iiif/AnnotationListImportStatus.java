package nl.knaw.huygens.alexandria.endpoint.iiif;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.Lists;
import nl.knaw.huygens.alexandria.api.model.ProcessStatus;

import java.util.List;
import java.util.Map;

public class AnnotationListImportStatus extends ProcessStatus {
  private Map<String, Object> processedList;
  private final List<String> errors = Lists.newArrayList();

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
