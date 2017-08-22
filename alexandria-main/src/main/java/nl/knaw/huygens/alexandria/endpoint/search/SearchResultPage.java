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

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;

@JsonInclude(Include.NON_NULL)
@JsonTypeName("searchResultPage")
class SearchResultPage extends JsonWrapperObject {
  private final int pageNumber;
  @JsonIgnore
  private final String baseURI;
  @JsonIgnore
  private final boolean isLast;
  @JsonIgnore
  private List<Map<String, Object>> recordList;
  @JsonIgnore
  private final AtomicInteger counter;
  @JsonIgnore
  private final int lastPageNumber;

  private final Map<String, Object> searchInfo;

  public SearchResultPage(String baseURI, int pageNumber, SearchResult searchResult) {
    this.baseURI = baseURI;
    this.pageNumber = pageNumber;
    this.lastPageNumber = Math.max(1, searchResult.getTotalPages());
    this.isLast = pageNumber == lastPageNumber;
    this.counter = new AtomicInteger(searchResult.getPageSize() * (pageNumber - 1));
    Function<Map<String, Object>, Integer> counterFunction = t -> counter.incrementAndGet();
    this.searchInfo = ImmutableMap.<String, Object> builder()//
        .put("id", searchResult.getId())//
        .put("query", searchResult.getQuery())//
        .put("searchDurationInMs", searchResult.getSearchDurationInMs())//
        .put("totalPages", searchResult.getTotalPages())//
        .put("pageSize", searchResult.getPageSize())//
        .put("totalResults", searchResult.getTotalResults())//
        .build();
  }

  @JsonProperty(PropertyPrefix.LINK + "previousPage")
  public URI getPreviousPage() {
    return pageNumber > 1 ? URI.create(baseURI + (pageNumber - 1)) : null;
  }

  @JsonProperty(PropertyPrefix.LINK + "firstPage")
  public URI getFirstPage() {
    return URI.create(baseURI + 1);
  }

  @JsonProperty(PropertyPrefix.LINK + "nextPage")
  public URI getNextPage() {
    return isLast ? null : URI.create(baseURI + (pageNumber + 1));
  }

  @JsonProperty(PropertyPrefix.LINK + "lastPage")
  public URI getLastPage() {
    return URI.create(baseURI + (lastPageNumber));
  }

  public void setResults(List<Map<String, Object>> results) {
    recordList = results.stream().peek(r -> r.put("_resultNumber", counter.incrementAndGet())).collect(toList());
  }

  public List<Map<String, Object>> getRecords() {
    return recordList;
  }

  public Map<String, Object> getSearchInfo() {
    return searchInfo;
  }
}
