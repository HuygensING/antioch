package nl.knaw.huygens.alexandria.endpoint.webannotation;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import nl.knaw.huygens.alexandria.model.AlexandriaResource;

public class AlexandriaResourceCache {
  static Cache<String, Optional<AlexandriaResource>> cache = CacheBuilder.newBuilder()//
      .maximumSize(1000)//
      .build();

  public void add(AlexandriaResource resource) {
    cache.put(resource.getCargo(), Optional.of(resource));
  }

  static final Callable<Optional<AlexandriaResource>> EMPTY_WHEN_MISSING = Optional::empty;

  public Optional<AlexandriaResource> get(String cargo) {
    Optional<AlexandriaResource> oResource = Optional.empty();
    try {
      oResource = cache.get(cargo, EMPTY_WHEN_MISSING);
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return oResource;
  }

}
