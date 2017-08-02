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

import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import nl.knaw.huygens.alexandria.api.model.iiif.IIIFAnnotationList;
import nl.knaw.huygens.alexandria.endpoint.webannotation.WebAnnotation;
import nl.knaw.huygens.alexandria.endpoint.webannotation.WebAnnotationService;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnnotationListImportTask implements Runnable {
  private IIIFAnnotationList annotationList;
  private final WebAnnotationService webAnnotationService;
  private AnnotationListImportStatus status;

  public AnnotationListImportTask(IIIFAnnotationList annotationList, WebAnnotationService webAnnotationService) {
    this.annotationList = annotationList;
    this.webAnnotationService = webAnnotationService;
    this.status = new AnnotationListImportStatus();
  }

  @Override
  public void run() {
    status.setStarted();
    try {
      Map<String, Object> otherProperties = annotationList.getOtherProperties();
      String context = annotationList.getContext();
      status.setProcessedList(Maps.newHashMap(otherProperties));
      List<Map<String, Object>> resources = new ArrayList<>(annotationList.getResources().size());
      annotationList.getResources().forEach(prototype -> {
        prototype.setCreated(Instant.now().toString());
        prototype.getVariablePart().put("@context", context);
        WebAnnotation webAnnotation = webAnnotationService.validateAndStore(prototype);
        try {
          Map<String, Object> map = (Map<String, Object>) JsonUtils.fromString(webAnnotation.json());
          resources.add(map);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
      status.getProcessedList().put("resources", resources);
    } catch (Exception e){
      e.printStackTrace();
      status.getErrors().add("Exception thrown: " + e);
      status.getErrors().add("Exception message: " + e.getMessage());
      status.getErrors().add("Stacktrace: " + Joiner.on("\n").join(e.getStackTrace()));
    }
    status.setDone();
  }

  public AnnotationListImportStatus getStatus() {
    return status;
  }
}
