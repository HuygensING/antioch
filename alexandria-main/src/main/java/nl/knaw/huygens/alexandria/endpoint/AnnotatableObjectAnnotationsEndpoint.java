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

import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.ImmutableMap;

import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntity;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationPrototype;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.AlexandriaService;
import nl.knaw.huygens.alexandria.textlocator.AlexandriaTextLocator;
import nl.knaw.huygens.alexandria.textlocator.TextLocatorFactory;
import nl.knaw.huygens.alexandria.textlocator.TextLocatorParseException;

public abstract class AnnotatableObjectAnnotationsEndpoint extends JSONEndpoint {

  protected final AlexandriaService service;
  protected final LocationBuilder locationBuilder;
  protected final AnnotationCreationRequestBuilder requestBuilder;
  protected final UUID uuid;

  protected AnnotatableObjectAnnotationsEndpoint(AlexandriaService service, //
      AnnotationCreationRequestBuilder requestBuilder, //
      LocationBuilder locationBuilder, //
      final UUIDParam uuidParam) {
    Log.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
    this.service = service;
    this.requestBuilder = requestBuilder;
    this.uuid = uuidParam.getValue();
    this.locationBuilder = locationBuilder;

    // throw exception if annotatableObject doesn't exist or is tentative
    @SuppressWarnings("unused")
    AbstractAnnotatable annotatableObject = getAnnotatableObject();
  }

  protected abstract AbstractAnnotatable getAnnotatableObject();

  protected abstract AnnotationCreationRequestBuilder getAnnotationCreationRequestBuilder();

  @GET
  @ApiOperation(value = "get the (non-deleted, non-deprecated) annotations", response = AnnotationEntity.class)
  public Response get() {
    final Set<AnnotationEntity> annotationEntities = getAnnotatableObject().getAnnotations().stream()//
        .filter(AlexandriaAnnotation::isActive)//
        .map((AlexandriaAnnotation a) -> AnnotationEntity.of(a).withLocationBuilder(locationBuilder))//
        .collect(toSet());
    Map<String, Set<AnnotationEntity>> entity = ImmutableMap.of("annotations", annotationEntities);
    return Response.ok(entity).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("add annotation")
  public Response addAnnotation(@NotNull @Valid AnnotationPrototype prototype) {
    validateLocator(prototype);
    prototype.setState(AlexandriaState.TENTATIVE);
    AnnotationCreationRequest request = getAnnotationCreationRequestBuilder().build(prototype);
    AlexandriaAnnotation annotation = request.execute(service);
    return Response.created(locationBuilder.locationOf(annotation)).build();
  }

  private void validateLocator(AnnotationPrototype prototype) {
    if (prototype.getLocator().isPresent()) {
      if (!(getAnnotatableObject() instanceof AlexandriaResource)) {
        throw new BadRequestException("locators are only allowed on resource annotations");
      }

      AlexandriaResource resource = (AlexandriaResource) getAnnotatableObject();
      if (!resource.hasText()) {
        throw new BadRequestException("The resource has no attached text to use the locator on.");
      }

      String locatorString = prototype.getLocator().get();
      try {
        TextLocatorFactory textLocatorFactory = new TextLocatorFactory(service);
        AlexandriaTextLocator locator = textLocatorFactory.fromString(locatorString);
        textLocatorFactory.validate(locator, resource);
      } catch (TextLocatorParseException e) {
        throw new BadRequestException(e.getMessage());
      }
    }

  }

}
