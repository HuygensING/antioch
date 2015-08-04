package nl.knaw.huygens.alexandria.endpoint.search;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class SearchResultCache {
  static Cache<UUID, Optional<SearchResult>> cache = CacheBuilder.newBuilder()//
      .maximumSize(1000)//
      .build();

  public static void add(SearchResult searchResult) {
    cache.put(searchResult.getId(), Optional.ofNullable(searchResult));
  }

  static final Callable<Optional<SearchResult>> EMPTY_WHEN_MISSING = () -> Optional.empty();

  public static Optional<SearchResult> get(UUID id) {
    Optional<SearchResult> oSearchResult = Optional.empty();
    try {
      oSearchResult = cache.get(id, EMPTY_WHEN_MISSING);
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return oSearchResult;
  }

}
