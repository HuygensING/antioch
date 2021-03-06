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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
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
import nl.knaw.huygens.antioch.api.JsonTypeNames;
import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.endpoint.annotation.AnnotationEntity;
import nl.knaw.huygens.antioch.endpoint.annotation.AnnotationPrototype;
import nl.knaw.huygens.antioch.model.AbstractAnnotatable;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.service.AntiochService;
import nl.knaw.huygens.antioch.textlocator.AntiochTextLocator;
import nl.knaw.huygens.antioch.textlocator.TextLocatorFactory;
import nl.knaw.huygens.antioch.textlocator.TextLocatorParseException;

public abstract class AnnotatableObjectAnnotationsEndpoint extends JSONEndpoint {

  protected final AntiochService service;
  private final LocationBuilder locationBuilder;
  protected final AnnotationCreationRequestBuilder requestBuilder;
  protected final UUID uuid;

  protected AnnotatableObjectAnnotationsEndpoint(AntiochService service, //
      AnnotationCreationRequestBuilder requestBuilder, //
      LocationBuilder locationBuilder, //
      final UUIDParam uuidParam) {
    // Log.trace("resourceService=[{}], uuidParam=[{}]", service, uuidParam);
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
    final List<AnnotationEntity> annotationEntities = getAnnotatableObject().getAnnotations().stream()//
        .filter(AntiochAnnotation::isActive)//
        .sorted()//
        .map((AntiochAnnotation a) -> AnnotationEntity.of(a).withLocationBuilder(locationBuilder))//
        .collect(toList());
    return ok(jsonWrap(annotationEntities));
  }

  private Map<String, List<Map<String, AnnotationEntity>>> jsonWrap(final List<AnnotationEntity> annotationEntities) {
    return ImmutableMap.of(//
        JsonTypeNames.ANNOTATIONLIST,
        annotationEntities.stream()//
            .map(this::toAnnotationMap)//
            .collect(toList())//
    );
  }

  private Map<String, AnnotationEntity> toAnnotationMap(AnnotationEntity e) {
    return ImmutableMap.of(JsonTypeNames.ANNOTATION, e);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("add annotation")
  public Response addAnnotation(@NotNull @Valid AnnotationPrototype prototype) {
    validateLocator(prototype);
    prototype.setState(AntiochState.TENTATIVE);
    AnnotationCreationRequest request = getAnnotationCreationRequestBuilder().build(prototype);
    AntiochAnnotation annotation = request.execute(service);
    return created(locationBuilder.locationOf(annotation));
  }

  private void validateLocator(AnnotationPrototype prototype) {
    if (prototype.getLocator().isPresent()) {
      if (!(getAnnotatableObject() instanceof AntiochResource)) {
        throw new BadRequestException("locators are only allowed on resource annotations");
      }

      AntiochResource resource = (AntiochResource) getAnnotatableObject();
      if (!resource.hasText()) {
        throw new BadRequestException("The resource has no attached text to use the locator on.");
      }

      String locatorString = prototype.getLocator().get();
      try {
        TextLocatorFactory textLocatorFactory = new TextLocatorFactory(service);
        AntiochTextLocator locator = textLocatorFactory.fromString(locatorString);
        textLocatorFactory.validate(locator, resource);
      } catch (TextLocatorParseException e) {
        throw new BadRequestException(e.getMessage());
      }
    }

  }

}
