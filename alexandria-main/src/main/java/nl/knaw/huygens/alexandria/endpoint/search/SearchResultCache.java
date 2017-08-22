package nl.knaw.huygens.alexandria.endpoint.search;

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

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

class SearchResultCache {
  private static final Cache<UUID, Optional<SearchResult>> cache = CacheBuilder.newBuilder()//
      .maximumSize(1000)//
      .build();

  public static void add(SearchResult searchResult) {
    cache.put(searchResult.getId(), Optional.of(searchResult));
  }

  private static final Callable<Optional<SearchResult>> EMPTY_WHEN_MISSING = Optional::empty;

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
