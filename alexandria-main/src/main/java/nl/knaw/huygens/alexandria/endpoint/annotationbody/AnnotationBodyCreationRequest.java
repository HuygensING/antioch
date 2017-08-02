package nl.knaw.huygens.alexandria.endpoint.annotationbody;

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

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.CreationRequest;
import nl.knaw.huygens.alexandria.jaxrs.ThreadContext;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationBodyCreationRequest implements CreationRequest<AlexandriaAnnotationBody> {
  private final AnnotationBodyPrototype prototype;

  private boolean wasCreated;

  public AnnotationBodyCreationRequest(AnnotationBodyPrototype prototype) {
    this.prototype = prototype;
  }

  @Override
  public AlexandriaAnnotationBody execute(AlexandriaService service) {
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(ThreadContext.getUserName(), Instant.now(), AlexandriaProvenance.DEFAULT_WHY);
    return service.createAnnotationBody(providedUUID(), providedType().orElse(""), providedValue(), provenance);
  }

  private String providedValue() {
    return requireNonNull(prototype.getValue(), "Required 'value' field was not validated for being non-null");
  }

  private Optional<String> providedType() {
    return prototype.getType();
  }

  public boolean wasCreated() {
    return wasCreated;
  }

  private UUID providedUUID() {
    return prototype.getId().getValue();
  }

}
