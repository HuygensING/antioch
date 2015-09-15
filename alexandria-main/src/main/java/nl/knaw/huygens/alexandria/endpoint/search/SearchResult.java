package nl.knaw.huygens.alexandria.endpoint.search;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.collect.Lists;

import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.Identifiable;

@JsonInclude(Include.NON_NULL)
public class SearchResult implements Identifiable {
  static final double PAGESIZE = 10;
  private UUID id;
  private AlexandriaQuery query;
  private List<Map<String, Object>> results = Lists.newArrayList();
  @JsonIgnore
  private LocationBuilder locationBuilder;

  @Inject
  public SearchResult(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @JsonTypeInfo(use = Id.NAME, include = As.EXISTING_PROPERTY)
  public AlexandriaQuery getQuery() {
    return query;
  }

  public void setQuery(AlexandriaQuery query) {
    this.query = query;
  }

  public int getTotalResults() {
    return results.size();
  }

  public int getTotalResultPages() {
    return (int) Math.ceil(getTotalResults() / PAGESIZE);
  }

  @JsonIgnore
  public List<Map<String, Object>> getResults() {
    return results;
  }

  public void setResults(List<Map<String, Object>> results) {
    this.results = results;
  }

  public URI getFirstResultPage() {
    return getTotalResults() > 0 ? URI.create(locationBuilder.locationOf(this) + "/resultPages/1") : null;
  }

  @JsonIgnore
  public List<Map<String, Object>> getRecordsForPage(int pageNum) {
    if (pageNum < 1 || pageNum > getTotalResultPages()) {
      return Lists.newArrayList();
    }
    int fromIndex = (int) ((pageNum - 1) * PAGESIZE);
    int toIndex = Math.min((int) (fromIndex + PAGESIZE), getTotalResults());
    return results.subList(fromIndex, toIndex);
  }

}
