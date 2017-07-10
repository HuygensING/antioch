package nl.knaw.huygens.alexandria.api.model.search;

/*
 * #%L
 * alexandria-api
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
