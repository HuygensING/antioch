package nl.knaw.huygens.alexandria.endpoint.search;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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
  static final double PAGE_SIZE = 10;
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
    return (int) Math.ceil(getTotalResults() / PAGE_SIZE);
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
  public List<Map<String, Object>> getRecordsForPage(int pageNumber) {
    if (pageNumber < 1 || pageNumber > getTotalResultPages()) {
      return Lists.newArrayList();
    }
    int fromIndex = (int) ((pageNumber - 1) * PAGE_SIZE);
    int toIndex = Math.min((int) (fromIndex + PAGE_SIZE), getTotalResults());
    return results.subList(fromIndex, toIndex);
  }

}
