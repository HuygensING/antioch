package nl.knaw.huygens.antioch.endpoint;

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

import javax.ws.rs.BadRequestException;

import nl.knaw.huygens.antioch.endpoint.annotation.AnnotationPrototype;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.model.TentativeAntiochProvenance;
import nl.knaw.huygens.antioch.service.AntiochService;
import nl.knaw.huygens.antioch.textlocator.AntiochTextLocator;
import nl.knaw.huygens.antioch.textlocator.TextLocatorFactory;
import nl.knaw.huygens.antioch.textlocator.TextLocatorParseException;

public class AnnotationCreationRequest implements CreationRequest<AntiochAnnotation> {

  private final AnnotationPrototype prototype;

  private Optional<AntiochResource> resource = Optional.empty();
  private Optional<AntiochAnnotation> annotation = Optional.empty();

  public AnnotationCreationRequest(AntiochResource resource, AnnotationPrototype prototype) {
    this.prototype = prototype;
    this.resource = Optional.of(resource);
  }

  public AnnotationCreationRequest(AntiochAnnotation annotation, AnnotationPrototype prototype) {
    this.prototype = prototype;
    this.annotation = Optional.of(annotation);
  }

  @Override
  public AntiochAnnotation execute(AntiochService service) {
    final TentativeAntiochProvenance provenance = providedProvenance().orElse(TentativeAntiochProvenance.createDefault());

    UUID annotationBodyUuid = UUID.randomUUID();
    String type = providedType().orElse("");
    Optional<AntiochAnnotationBody> result = service.findAnnotationBodyWithTypeAndValue(type, providedValue());
    // AntiochState state = prototype.getState();
    AntiochAnnotationBody body = result//
        .orElseGet(() -> service.createAnnotationBody(annotationBodyUuid, type, providedValue(), provenance));

    if (resource.isPresent()) {
      if (prototype.getLocator().isPresent()) {
        String textLocatorString = prototype.getLocator().get();
        AntiochTextLocator textLocator;
        try {
          textLocator = new TextLocatorFactory(service).fromString(textLocatorString);
          return service.annotate(resource.get(), textLocator, body, provenance);
        } catch (TextLocatorParseException e) {
          throw new BadRequestException(e.getMessage());
        }
      }
      return service.annotate(resource.get(), body, provenance);
    }

    return service.annotate(annotation.get(), body, provenance);
  }

  // private UUIDParam providedId() {
  // return requireNonNull(prototype.getAnnotationBodyId(), "Required 'value' field was not validated for being non-null");
  // }

  private Optional<String> providedType() {
    return prototype.getType();
  }

  private String providedValue() {
    return prototype.getValue();
  }

  private Optional<TentativeAntiochProvenance> providedProvenance() {
    return prototype.getProvenance().map(ProvenancePrototype::getValue);
  }

}
