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

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.stream.Stream;

import javax.inject.Inject;

import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationDeprecationRequestBuilder {
  private final AlexandriaService service;
  private AlexandriaAnnotation originalAnnotation;

  @Inject
  public AnnotationDeprecationRequestBuilder(AlexandriaService service) {
    this.service = requireNonNull(service, "AlexandriaService MUST not be null");
  }

  public AnnotationDeprecationRequestBuilder ofAnnotation(AlexandriaAnnotation annotation) {
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
