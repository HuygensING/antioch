package nl.knaw.huygens.alexandria.endpoint.search;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import nl.knaw.huygens.alexandria.model.Identifiable;

public class SearchResult implements Identifiable {

  private UUID id;
  private AlexandriaQuery query;

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

}
