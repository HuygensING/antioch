package nl.knaw.huygens.antioch.endpoint.annotation;

/*
 * #%L
 * antioch-main
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

import javax.inject.Inject;
import javax.ws.rs.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.huygens.antioch.endpoint.AnnotatableObjectAnnotationsEndpoint;
import nl.knaw.huygens.antioch.endpoint.AnnotationCreationRequestBuilder;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.endpoint.UUIDParam;
import nl.knaw.huygens.antioch.model.AbstractAnnotatable;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.service.AntiochService;

public class AnnotationAnnotationsEndpoint extends AnnotatableObjectAnnotationsEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(AnnotationAnnotationsEndpoint.class);

  // TODO: how to remove this duplicated inject/constructor?
  @Inject
  public AnnotationAnnotationsEndpoint(AntiochService service, //
      AnnotationCreationRequestBuilder requestBuilder, //
      LocationBuilder locationBuilder, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    super(service, requestBuilder, locationBuilder, uuidParam);
    LOG.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
  }

  @Override
  protected AbstractAnnotatable getAnnotatableObject() {
    AntiochAnnotation annotation = service.readAnnotation(uuid)//
        .orElseThrow(AnnotationsEndpoint.annotationNotFoundForId(uuid));
    if (annotation.isTentative()) {
      throw AnnotationsEndpoint.annotationIsTentative(uuid);
    }
    return annotation;
  }

  @Override
  protected AnnotationCreationRequestBuilder getAnnotationCreationRequestBuilder() {
    return requestBuilder.ofAnnotation(uuid);
  }

}
