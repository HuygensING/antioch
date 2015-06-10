package nl.knaw.huygens.alexandria.endpoint;

import java.util.Map;

import com.google.common.collect.Maps;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.Accountable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public class EndpointPathResolver {
  private Map<Class<? extends Accountable>, String> accountableEndpoints;

  public EndpointPathResolver() {
    Log.trace("EndpointPathResolver created");
    accountableEndpoints = Maps.newHashMap();
    accountableEndpoints.put(AlexandriaAnnotation.class, EndpointPaths.ANNOTATIONS);
    accountableEndpoints.put(AlexandriaAnnotationBody.class, EndpointPaths.ANNOTATIONBODIES);
    accountableEndpoints.put(AlexandriaResource.class, EndpointPaths.RESOURCES);
  }

  public String pathOf(Accountable accountable) {
    return accountableEndpoints.get(accountable.getClass());
  }

}
