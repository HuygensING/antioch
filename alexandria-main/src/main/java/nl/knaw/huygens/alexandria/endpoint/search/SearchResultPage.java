package nl.knaw.huygens.alexandria.endpoint.search;

/*
 * #%L
 * alexandria-main
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
public class SearchResultPage extends JsonWrapperObject {
  private int pageNumber;
  @JsonIgnore
  private String baseURI;
  @JsonIgnore
  private boolean isLast;
  @JsonIgnore
  private List<Map<String, Object>> recordList;
  @JsonIgnore
  private Function<Map<String, Object>, Integer> counterFunction;
  @JsonIgnore
  private AtomicInteger counter;
  @JsonIgnore
  private int lastPageNumber;

  private Map<String, Object> searchInfo;

  public SearchResultPage(String baseURI, int pageNumber, SearchResult searchResult) {
    this.baseURI = baseURI;
    this.pageNumber = pageNumber;
    this.lastPageNumber = Math.max(1, searchResult.getTotalPages());
    this.isLast = pageNumber == lastPageNumber;
    this.counter = new AtomicInteger(searchResult.getPageSize() * (pageNumber - 1));
    this.counterFunction = t -> counter.incrementAndGet();
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
    recordList = results.stream().map(r -> {
      r.put("_resultNumber", counter.incrementAndGet());
      return r;
    }).collect(toList());
  }

  public List<Map<String, Object>> getRecords() {
    return recordList;
  }

  public Map<String, Object> getSearchInfo() {
    return searchInfo;
  }
}
