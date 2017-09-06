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

import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.antioch.endpoint.CreationRequest;
import nl.knaw.huygens.antioch.endpoint.ProvenancePrototype;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;
import nl.knaw.huygens.antioch.model.TentativeAntiochProvenance;
import nl.knaw.huygens.antioch.service.AntiochService;

public class AnnotationDeprecationRequest implements CreationRequest<AntiochAnnotation> {

  private final AnnotationPrototype prototype;

  private final AntiochAnnotation originalAnnotation;

  public AnnotationDeprecationRequest(AntiochAnnotation originalAnnotation, AnnotationPrototype prototype) {
    this.prototype = prototype;
    this.originalAnnotation = originalAnnotation;
  }

  @Override
  public AntiochAnnotation execute(AntiochService service) {
    final TentativeAntiochProvenance provenance = providedProvenance().orElse(TentativeAntiochProvenance.createDefault());
    String type = originalAnnotation.getBody().getType();
    AntiochAnnotationBody body = service.findAnnotationBodyWithTypeAndValue(type, providedValue())//
        .orElseGet(() -> new AntiochAnnotationBody(UUID.randomUUID(), type, providedValue(), provenance));
    AntiochAnnotation newAnnotation = new AntiochAnnotation(originalAnnotation.getId(), body, provenance);

    return service.deprecateAnnotation(originalAnnotation.getId(), newAnnotation);
  }

  private String providedValue() {
    return prototype.getValue();
  }

  private Optional<TentativeAntiochProvenance> providedProvenance() {
    return prototype.getProvenance().map(ProvenancePrototype::getValue);
  }

}
