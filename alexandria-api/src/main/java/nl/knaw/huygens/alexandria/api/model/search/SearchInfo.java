package nl.knaw.huygens.alexandria.api.model.search;

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
