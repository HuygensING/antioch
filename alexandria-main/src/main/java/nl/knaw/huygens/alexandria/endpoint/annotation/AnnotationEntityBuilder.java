package nl.knaw.huygens.alexandria.endpoint.annotation;

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

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;

public class AnnotationEntityBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(AnnotationEntityBuilder.class);
  private LocationBuilder locationBuilder;

  @Inject
  public AnnotationEntityBuilder(LocationBuilder locationBuilder) {
    LOG.trace("AnnotationEntityBuilder created: locationBuilder=[{}]", locationBuilder);
    this.locationBuilder = locationBuilder;
  }

  public AnnotationEntity build(AlexandriaAnnotation annotation) {
    return AnnotationEntity.of(annotation).withLocationBuilder(locationBuilder);
  }

}
