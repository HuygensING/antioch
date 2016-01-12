package nl.knaw.huygens.alexandria.endpoint.annotation;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Optional;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.CreationRequest;
import nl.knaw.huygens.alexandria.endpoint.ProvenancePrototype;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationDeprecationRequest implements CreationRequest<AlexandriaAnnotation> {

  private final AnnotationPrototype prototype;

  private AlexandriaAnnotation originalAnnotation;

  public AnnotationDeprecationRequest(AlexandriaAnnotation originalAnnotation, AnnotationPrototype prototype) {
    this.prototype = prototype;
    this.originalAnnotation = originalAnnotation;
  }

  @Override
  public AlexandriaAnnotation execute(AlexandriaService service) {
    final TentativeAlexandriaProvenance provenance = providedProvenance().orElse(TentativeAlexandriaProvenance.createDefault());
    String type = originalAnnotation.getBody().getType();
    AlexandriaAnnotationBody body = service.findAnnotationBodyWithTypeAndValue(type, providedValue())//
        .orElseGet(() -> new AlexandriaAnnotationBody(UUID.randomUUID(), type, providedValue(), provenance));
    AlexandriaAnnotation newAnnotation = new AlexandriaAnnotation(originalAnnotation.getId(), body, provenance);

    return service.deprecateAnnotation(originalAnnotation.getId(), newAnnotation);
  }

  private String providedValue() {
    return prototype.getValue();
  }

  private Optional<TentativeAlexandriaProvenance> providedProvenance() {
    return prototype.getProvenance().map(ProvenancePrototype::getValue);
  }

}
