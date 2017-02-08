package nl.knaw.huygens.alexandria.api.model.search;

/*
 * #%L
 * alexandria-api
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SearchInfo {
  UUID id;
  Long searchDurationInMs;
  int totalPages;
  int pageSize;
  int totalResults;

  Object query; // Object, because AlexandriaQuery is wrapped in {"query":{...}} because it's a JsonWrapper

  public UUID getId() {
    return id;
  }

  public SearchInfo setId(final UUID id) {
    this.id = id;
    return this;
  }

  @JsonIgnore
  public AlexandriaQuery getAlexandriaQuery() {
    return (AlexandriaQuery) query;
  }

  public Object getQuery() {
    return query;
  }

  public SearchInfo setQuery(final Object query) {
    this.query = query;
    return this;
  }

  public Long getSearchDurationInMs() {
    return searchDurationInMs;
  }

  public SearchInfo setSearchDurationInMs(final Long searchDurationInMs) {
    this.searchDurationInMs = searchDurationInMs;
    return this;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public SearchInfo setTotalPages(final int totalPages) {
    this.totalPages = totalPages;
    return this;
  }

  public int getPageSize() {
    return pageSize;
  }

  public SearchInfo setPageSize(final int pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  public int getTotalResults() {
    return totalResults;
  }

  public SearchInfo setTotalResults(final int totalResults) {
    this.totalResults = totalResults;
    return this;
  }
}
