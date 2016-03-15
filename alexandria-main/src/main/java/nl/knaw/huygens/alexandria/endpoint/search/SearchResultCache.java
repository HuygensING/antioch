package nl.knaw.huygens.alexandria.endpoint.search;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
    cache.put(searchResult.getId(), Optional.of(searchResult));
  }

  static final Callable<Optional<SearchResult>> EMPTY_WHEN_MISSING = Optional::empty;

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
