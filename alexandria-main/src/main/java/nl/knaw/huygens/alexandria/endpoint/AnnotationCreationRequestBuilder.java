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

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationPrototype;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationCreationRequestBuilder {
  public static final String MISSING_TYPE_MESSAGE = "Annotation MUST have a type";
  public static final String NO_SUCH_ANNOTATION_FORMAT = "Supposedly existing annotation [%s] not found";
  public static final String MISSING_ANNOTATION_BODY_MESSAGE = "Missing or empty annotation request body";

  // public static AnnotationCreationRequestBuilder servedBy(AlexandriaService service) {
  // return new AnnotationCreationRequestBuilder(service);
  // }

  private final AlexandriaService service;

  private Optional<AlexandriaResource> resource = Optional.empty();
  private Optional<AlexandriaAnnotation> annotation = Optional.empty();

  @Inject
  public AnnotationCreationRequestBuilder(AlexandriaService service) {
    this.service = requireNonNull(service, "AlexandriaService MUST not be null");
  }

  public AnnotationCreationRequestBuilder ofResource(UUID uuid) {
    resource = service.readResource(uuid);
    return this;
  }

  public AnnotationCreationRequestBuilder ofAnnotation(UUID uuid) {
    annotation = service.readAnnotation(uuid);
    return this;
  }

  public AnnotationCreationRequest build(AnnotationPrototype prototype) {
    // Optional.ofNullable(prototype).orElseThrow(missingBodyException());

    // validateId(prototype);
    // validateType(prototype);
    // validateValue(prototype);
    // validateCreatedOn(prototype);
    // validateAnnotations(prototype);

    if (resource.isPresent()) {
      return new AnnotationCreationRequest(resource.get(), prototype);
    }

    return new AnnotationCreationRequest(annotation.get(), prototype);
  }

  protected void validateId(AnnotationPrototype prototype) {
    Log.trace("Validating id");
    return; // missing or empty Id means client does not override server
            // generated value.
  }

  protected void validateType(AnnotationPrototype prototype) {
    Log.trace("Validating type");
    // prototype.getType().filter(s -> !s.isEmpty()).orElseThrow(missingTypeException());
  }

  protected void validateValue(AnnotationPrototype prototype) {
    Log.trace("Validating value");
    return; // missing or empty value is ok (means annotation is a Tag)
  }

  protected void validateProvenance(AnnotationPrototype prototype) {
    Log.trace("Validating provenance");
    return; // missing or empty provenance means client does not override server
            // set value.
  }

  // protected void validateAnnotations(AnnotationPrototype prototype) {
  // Log.trace("Validating annotations");
  // prototype.getAnnotations().ifPresent(annotationParams ->
  // stream(annotationParams) //
  // .map(UUIDParam::getValue).forEach(this::validateAnnotationId));
  // }

  protected <T> Stream<T> stream(Collection<T> c) {
    return c.parallelStream(); // override in case you prefer stream() over
                               // parallelStream()
  }

  protected void validateAnnotationId(UUID uuid) {
    Log.trace("Validating annotation: [{}]", uuid);
    Optional.ofNullable(service.readAnnotation(uuid)).orElseThrow(noSuchAnnotationException(uuid));
  }

  protected Supplier<BadRequestException> missingBodyException() {
    return () -> badRequestException(MISSING_ANNOTATION_BODY_MESSAGE);
  }

  protected Supplier<BadRequestException> missingTypeException() {
    return () -> badRequestException(MISSING_TYPE_MESSAGE);
  }

  protected Supplier<BadRequestException> noSuchAnnotationException(UUID uuid) {
    return () -> badRequestException(String.format(NO_SUCH_ANNOTATION_FORMAT, uuid.toString()));
  }

  protected BadRequestException badRequestException(String message) {
    Log.trace(message);
    return new BadRequestException(message);
  }

}
