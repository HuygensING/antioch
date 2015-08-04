package nl.knaw.huygens.alexandria.endpoint;

import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

import com.google.common.collect.Maps;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.search.SearchResult;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.Identifiable;

@Singleton
public class EndpointPathResolver {
  private Map<Class<? extends Identifiable>, String> identifiableEndpoints;

  public EndpointPathResolver() {
    Log.trace("EndpointPathResolver created");
    identifiableEndpoints = Maps.newHashMap();
    identifiableEndpoints.put(AlexandriaAnnotation.class, EndpointPaths.ANNOTATIONS);
    identifiableEndpoints.put(AlexandriaAnnotationBody.class, EndpointPaths.ANNOTATIONBODIES);
    identifiableEndpoints.put(AlexandriaResource.class, EndpointPaths.RESOURCES);
    identifiableEndpoints.put(SearchResult.class, EndpointPaths.SEARCHES);
  }

  public Optional<String> pathOf(Identifiable accountable) {
    return pathOf(accountable.getClass());
  }

  public Optional<String> pathOf(Class<? extends Identifiable> accountableClass) {
    return Optional.ofNullable(identifiableEndpoints.get(accountableClass));
  }

}
