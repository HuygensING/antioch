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

  public SearchResultPage(String baseURI, int pageNumber, int lastPageNumber, int pageSize) {
    this.baseURI = baseURI;
    this.pageNumber = pageNumber;
    this.lastPageNumber = lastPageNumber;
    this.isLast = pageNumber == lastPageNumber;
    this.counter = new AtomicInteger(pageSize * (pageNumber - 1));
    this.counterFunction = t -> counter.incrementAndGet();
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
}
