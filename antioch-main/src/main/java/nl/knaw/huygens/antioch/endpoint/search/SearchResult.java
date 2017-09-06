package nl.knaw.huygens.antioch.endpoint.search;

/*
 * #%L
 * antioch-main
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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.collect.Lists;

import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.api.model.PropertyPrefix;
import nl.knaw.huygens.antioch.api.model.search.AntiochQuery;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.model.Identifiable;

@JsonInclude(Include.NON_NULL)
public class SearchResult implements Identifiable {
  private UUID id;
  private AntiochQuery query;
  private List<Map<String, Object>> results = Lists.newArrayList();
  private Long searchDurationInMs;

  @JsonIgnore
  private final LocationBuilder locationBuilder;

  @Inject
  public SearchResult(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
  }

  public SearchResult setId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @JsonTypeInfo(use = Id.NAME, include = As.EXISTING_PROPERTY)
  public AntiochQuery getQuery() {
    return query;
  }

  public SearchResult setQuery(AntiochQuery query) {
    this.query = query;
    return this;
  }

  public int getTotalResults() {
    return results.size();
  }

  public int getTotalPages() {
    return (int) Math.ceil(getTotalResults() / (double) query.getPageSize());
  }

  @JsonIgnore
  public List<Map<String, Object>> getResults() {
    return results;
  }

  public SearchResult setResults(List<Map<String, Object>> results) {
    this.results = results;
    return this;
  }

  @JsonProperty(PropertyPrefix.LINK + "firstPage")
  public URI getFirstResultPage() {
    return getTotalResults() > 0 ? locationBuilder.locationOf(this,EndpointPaths.RESULTPAGES , "1") : null;
  }

  @JsonIgnore
  public List<Map<String, Object>> getRecordsForPage(int pageNumber) {
    if (pageNumber < 1 || pageNumber > getTotalPages()) {
      return Lists.newArrayList();
    }
    int fromIndex = (pageNumber - 1) * getPageSize();
    int toIndex = Math.min(fromIndex + getPageSize(), getTotalResults());
    return results.subList(fromIndex, toIndex);
  }

  public int getPageSize() {
    return query.getPageSize();
  }

  public Long getSearchDurationInMs() {
    return searchDurationInMs;
  }

  public SearchResult setSearchDurationInMilliseconds(Long searchDurationInMs) {
    this.searchDurationInMs = searchDurationInMs;
    return this;
  }

}
