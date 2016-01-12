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

import javax.inject.Inject;
import javax.ws.rs.PathParam;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.AnnotatableObjectAnnotationsEndpoint;
import nl.knaw.huygens.alexandria.endpoint.AnnotationCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class AnnotationAnnotationsEndpoint extends AnnotatableObjectAnnotationsEndpoint {

  // TODO: how to remove this duplicated inject/constructor?
  @Inject
  public AnnotationAnnotationsEndpoint(AlexandriaService service, //
      AnnotationCreationRequestBuilder requestBuilder, //
      LocationBuilder locationBuilder, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    super(service, requestBuilder, locationBuilder, uuidParam);
    Log.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
  }

  @Override
  protected AbstractAnnotatable getAnnotatableObject() {
    AlexandriaAnnotation annotation = service.readAnnotation(uuid)//
        .orElseThrow(AnnotationsEndpoint.annotationNotFoundForId(uuid));
    if (annotation.isTentative()){
      throw AnnotationsEndpoint.annotationIsTentative(uuid);
    }
    return annotation;
  }

  @Override
  protected AnnotationCreationRequestBuilder getAnnotationCreationRequestBuilder() {
    return requestBuilder.ofAnnotation(uuid);
  }

}
