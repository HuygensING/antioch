package nl.knaw.huygens.alexandria.endpoint.search;

import nl.knaw.huygens.alexandria.endpoint.Entity;

public class SearchEntity implements Entity {
  private SearchResult searchResult;

  public static SearchEntity of(SearchResult searchResult) {
    SearchEntity searchEntity = new SearchEntity();
    searchEntity.setSearchResult(searchResult);
    return searchEntity;
  }

  public SearchResult getSearchResult() {
    return searchResult;
  }

  public void setSearchResult(SearchResult searchResult) {
    this.searchResult = searchResult;
  }

}
