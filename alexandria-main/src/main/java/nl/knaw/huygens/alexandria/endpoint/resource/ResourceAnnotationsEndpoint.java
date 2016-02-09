package nl.knaw.huygens.alexandria.endpoint.resource;

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

import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.google.common.collect.ImmutableMap;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.endpoint.AnnotatableObjectAnnotationsEndpoint;
import nl.knaw.huygens.alexandria.endpoint.AnnotationCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntity;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Api("annotations")
public class ResourceAnnotationsEndpoint extends AnnotatableObjectAnnotationsEndpoint {

  // TODO: how to remove this duplicated inject/constructor?
  @Inject
  public ResourceAnnotationsEndpoint(AlexandriaService service, //
      AnnotationCreationRequestBuilder requestBuilder, //
      LocationBuilder locationBuilder, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    super(service, requestBuilder, locationBuilder, uuidParam);
  }

  // TODO: we may need this casting more often, so migrate to better location if also needed elsewhere
  private static AlexandriaResource asResource(Accountable accountable) {
    return (AlexandriaResource) accountable;
  }

  @Override
  protected AbstractAnnotatable getAnnotatableObject() {
    AlexandriaResource resource = service.readResource(uuid)//
        .orElseThrow(ResourceValidatorFactory.resourceNotFoundForId(uuid));
    if (resource.isTentative()) {
      throw ResourceValidatorFactory.resourceIsTentativeException(uuid);
    }
    return resource;
  }

  @Override
  protected AnnotationCreationRequestBuilder getAnnotationCreationRequestBuilder() {
    return requestBuilder.ofResource(uuid);
  }

  @GET
  @ApiOperation(value = "get the annotations of this resource", response = AnnotationEntity.class)
  @Override
  public Response get() {
    Stream<AlexandriaAnnotation> resourceAnnotationsStream = getAnnotatableObject().getAnnotations().stream();

    final Set<AnnotationEntity> annotationEntities = resourceAnnotationsStream //
        .filter(AlexandriaAnnotation::isActive) //
        .map((AlexandriaAnnotation a) -> AnnotationEntity.of(a).withLocationBuilder(locationBuilder)) //
        .collect(toSet());

    final Map<String, Set<AnnotationEntity>> entity = ImmutableMap.of("annotations", annotationEntities);
    return Response.ok(entity).build();
  }

}
