package nl.knaw.huygens.alexandria.endpoint;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.alexandria.endpoint.search.SearchResult;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.Identifiable;

@Singleton
public class EndpointPathResolver {
  private static final Map<Class<? extends Identifiable>, String> IDENTIFIABLE_ENDPOINTS //
      = ImmutableMap.<Class<? extends Identifiable>, String>builder()
      .put(AlexandriaAnnotation.class, EndpointPaths.ANNOTATIONS)
      .put(AlexandriaAnnotationBody.class, EndpointPaths.ANNOTATIONBODIES)
      .put(AlexandriaResource.class, EndpointPaths.RESOURCES)
      .put(SearchResult.class, EndpointPaths.SEARCHES)
      .build();

  public Optional<String> pathOf(Class<? extends Identifiable> identifiableClass) {
    return Optional.ofNullable(IDENTIFIABLE_ENDPOINTS.get(identifiableClass));
  }

}
