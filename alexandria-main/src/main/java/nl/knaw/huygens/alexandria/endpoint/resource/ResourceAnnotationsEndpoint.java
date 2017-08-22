package nl.knaw.huygens.alexandria.endpoint.resource;

/*
 * #%L
 * alexandria-main
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
import nl.knaw.huygens.alexandria.endpoint.AnnotatableObjectAnnotationsEndpoint;
import nl.knaw.huygens.alexandria.endpoint.AnnotationCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntity;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.Accountable;
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
  @SuppressWarnings("unused")
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
    return super.get();
    // Stream<AlexandriaAnnotation> resourceAnnotationsStream = getAnnotatableObject().getAnnotations().stream();
    //
    // final Set<AnnotationEntity> annotationEntities = resourceAnnotationsStream //
    // .filter(AlexandriaAnnotation::isActive) //
    // .map((AlexandriaAnnotation a) -> AnnotationEntity.of(a).withLocationBuilder(locationBuilder)) //
    // .collect(toSet());
    //
    // final Map<String, Set<AnnotationEntity>> entity = ImmutableMap.of("annotations", annotationEntities);
    // return ok(entity);
  }

}
