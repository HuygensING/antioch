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

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.stream.Stream;

import javax.inject.Inject;

import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.service.AntiochService;

public class AnnotationDeprecationRequestBuilder {
  private final AntiochService service;
  private AntiochAnnotation originalAnnotation;

  @Inject
  public AnnotationDeprecationRequestBuilder(AntiochService service) {
    this.service = requireNonNull(service, "AntiochService MUST not be null");
  }

  public AnnotationDeprecationRequestBuilder ofAnnotation(AntiochAnnotation annotation) {
    this.originalAnnotation = annotation;
    return this;
  }

  public AnnotationDeprecationRequest build(AnnotationPrototype prototype) {
    return new AnnotationDeprecationRequest(originalAnnotation, prototype);
  }

  protected <T> Stream<T> stream(Collection<T> c) {
    return c.parallelStream(); // override in case you prefer stream() over parallelStream()
  }

}
