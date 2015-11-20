package nl.knaw.huygens.alexandria.endpoint.annotationbody;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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
