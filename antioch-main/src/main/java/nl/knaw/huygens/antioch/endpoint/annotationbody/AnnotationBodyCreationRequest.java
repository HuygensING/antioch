package nl.knaw.huygens.antioch.endpoint.annotationbody;

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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.antioch.endpoint.CreationRequest;
import nl.knaw.huygens.antioch.jaxrs.ThreadContext;
import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;
import nl.knaw.huygens.antioch.model.AntiochProvenance;
import nl.knaw.huygens.antioch.model.TentativeAntiochProvenance;
import nl.knaw.huygens.antioch.service.AntiochService;

public class AnnotationBodyCreationRequest implements CreationRequest<AntiochAnnotationBody> {
  private final AnnotationBodyPrototype prototype;

  private boolean wasCreated;

  public AnnotationBodyCreationRequest(AnnotationBodyPrototype prototype) {
    this.prototype = prototype;
  }

  @Override
  public AntiochAnnotationBody execute(AntiochService service) {
    TentativeAntiochProvenance provenance = new TentativeAntiochProvenance(ThreadContext.getUserName(), Instant.now(), AntiochProvenance.DEFAULT_WHY);
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
