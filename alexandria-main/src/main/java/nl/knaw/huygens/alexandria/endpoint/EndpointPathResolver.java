package nl.knaw.huygens.alexandria.endpoint;

import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

import com.google.common.collect.Maps;

@Singleton
public class EndpointPathResolver {
  private Map<Class<? extends Accountable>, String> accountableEndpoints;

  public EndpointPathResolver() {
    Log.trace("EndpointPathResolver created");
    accountableEndpoints = Maps.newHashMap();
    accountableEndpoints.put(AlexandriaAnnotation.class, EndpointPaths.ANNOTATIONS);
    accountableEndpoints.put(AlexandriaAnnotationBody.class, EndpointPaths.ANNOTATIONBODIES);
    accountableEndpoints.put(AlexandriaResource.class, EndpointPaths.RESOURCES);
  }

  public Optional<String> pathOf(Accountable accountable) {
    return pathOf(accountable.getClass());
  }

  public Optional<String> pathOf(Class<? extends Accountable> accountableClass) {
    return Optional.ofNullable(accountableEndpoints.get(accountableClass));
  }

}
