package nl.knaw.huygens.alexandria.endpoint.search;

public class SearchResultEntityBuilder {

  public SearchEntity build(SearchResult searchResult) {
    return SearchEntity.of(searchResult);
  }

}
