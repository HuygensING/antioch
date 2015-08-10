package nl.knaw.huygens.alexandria.endpoint.resource;

import static java.util.stream.Collectors.toSet;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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
import nl.knaw.huygens.alexandria.util.Optionals;

@Api("annotations")
public class ResourceAnnotationsEndpoint extends AnnotatableObjectAnnotationsEndpoint {

  // TODO: how to remove this duplicated inject/constructor?
  @Inject
  public ResourceAnnotationsEndpoint(AlexandriaService service,     //
                                     AnnotationCreationRequestBuilder requestBuilder,     //
                                     LocationBuilder locationBuilder,     //
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
        .orElseThrow(ResourcesEndpoint.resourceNotFoundForId(uuid));
    if (resource.isTentative()) {
      throw ResourcesEndpoint.resourceIsTentativeException(uuid);
    }
    return resource;
  }

  @Override
  protected AnnotationCreationRequestBuilder getAnnotationCreationRequestBuilder() {
    return requestBuilder.ofResource(uuid);
  }

  @GET
  @ApiOperation(value = "get the annotations of the resource and its immediate subresources", response = AnnotationEntity.class)
  @Override
  public Response get() {
    Stream<AlexandriaAnnotation> resourceAnnotationsStream = getAnnotatableObject().getAnnotations().stream();

    Stream<AlexandriaAnnotation> subresourceAnnotationsStream = asResource(getAnnotatableObject()) //
        .getSubResourcePointers().stream() //
        .map(service::dereference) //
        .flatMap(Optionals::stream) //
        .map(ResourceAnnotationsEndpoint::asResource) //
        .map(AlexandriaResource::getAnnotations) //
        .flatMap(Set::stream);

    Stream<AlexandriaAnnotation> joinedStream = Stream.concat(resourceAnnotationsStream, subresourceAnnotationsStream);

    final Set<AnnotationEntity> annotationEntities = joinedStream//
        .filter(AlexandriaAnnotation::isActive)//
        .map((AlexandriaAnnotation a) -> AnnotationEntity.of(a).withLocationBuilder(locationBuilder))//
        .collect(toSet());

    Map<String, Set<AnnotationEntity>> entity = ImmutableMap.of("annotations", annotationEntities);
    return Response.ok(entity).build();
  }

}
