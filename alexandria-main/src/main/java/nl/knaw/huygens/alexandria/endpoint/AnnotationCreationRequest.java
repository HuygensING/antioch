package nl.knaw.huygens.alexandria.endpoint;

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

import javax.ws.rs.BadRequestException;

import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationPrototype;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.textlocator.AlexandriaTextLocator;
import nl.knaw.huygens.alexandria.textlocator.TextLocatorFactory;
import nl.knaw.huygens.alexandria.textlocator.TextLocatorParseException;

public class AnnotationCreationRequest implements CreationRequest<AlexandriaAnnotation> {

  private final AnnotationPrototype prototype;

  private Optional<AlexandriaResource> resource = Optional.empty();
  private Optional<AlexandriaAnnotation> annotation = Optional.empty();

  public AnnotationCreationRequest(AlexandriaResource resource, AnnotationPrototype prototype) {
    this.prototype = prototype;
    this.resource = Optional.of(resource);
  }

  public AnnotationCreationRequest(AlexandriaAnnotation annotation, AnnotationPrototype prototype) {
    this.prototype = prototype;
    this.annotation = Optional.of(annotation);
  }

  @Override
  public AlexandriaAnnotation execute(AlexandriaService service) {
    final TentativeAlexandriaProvenance provenance = providedProvenance().orElse(TentativeAlexandriaProvenance.createDefault());

    UUID annotationBodyUuid = UUID.randomUUID();
    String type = providedType().orElse("");
    Optional<AlexandriaAnnotationBody> result = service.findAnnotationBodyWithTypeAndValue(type, providedValue());
    // AlexandriaState state = prototype.getState();
    AlexandriaAnnotationBody body = result//
        .orElseGet(() -> service.createAnnotationBody(annotationBodyUuid, type, providedValue(), provenance));

    if (resource.isPresent()) {
      if (prototype.getLocator().isPresent()) {
        String textLocatorString = prototype.getLocator().get();
        AlexandriaTextLocator textLocator;
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

  private Optional<TentativeAlexandriaProvenance> providedProvenance() {
    return prototype.getProvenance().map(ProvenancePrototype::getValue);
  }

}
