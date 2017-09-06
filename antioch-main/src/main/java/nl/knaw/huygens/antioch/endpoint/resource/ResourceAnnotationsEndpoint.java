package nl.knaw.huygens.antioch.endpoint.resource;

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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.antioch.endpoint.AnnotatableObjectAnnotationsEndpoint;
import nl.knaw.huygens.antioch.endpoint.AnnotationCreationRequestBuilder;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.endpoint.UUIDParam;
import nl.knaw.huygens.antioch.endpoint.annotation.AnnotationEntity;
import nl.knaw.huygens.antioch.model.AbstractAnnotatable;
import nl.knaw.huygens.antioch.model.Accountable;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.service.AntiochService;

@Api("annotations")
public class ResourceAnnotationsEndpoint extends AnnotatableObjectAnnotationsEndpoint {

  // TODO: how to remove this duplicated inject/constructor?
  @Inject
  public ResourceAnnotationsEndpoint(AntiochService service, //
      AnnotationCreationRequestBuilder requestBuilder, //
      LocationBuilder locationBuilder, //
      @PathParam("uuid") final UUIDParam uuidParam) {
    super(service, requestBuilder, locationBuilder, uuidParam);
  }

  // TODO: we may need this casting more often, so migrate to better location if also needed elsewhere
  @SuppressWarnings("unused")
  private static AntiochResource asResource(Accountable accountable) {
    return (AntiochResource) accountable;
  }

  @Override
  protected AbstractAnnotatable getAnnotatableObject() {
    AntiochResource resource = service.readResource(uuid)//
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
    return super.get();
    // Stream<AntiochAnnotation> resourceAnnotationsStream = getAnnotatableObject().getAnnotations().stream();
    //
    // final Set<AnnotationEntity> annotationEntities = resourceAnnotationsStream //
    // .filter(AntiochAnnotation::isActive) //
    // .map((AntiochAnnotation a) -> AnnotationEntity.of(a).withLocationBuilder(locationBuilder)) //
    // .collect(toSet());
    //
    // final Map<String, Set<AnnotationEntity>> entity = ImmutableMap.of("annotations", annotationEntities);
    // return ok(entity);
  }

}
