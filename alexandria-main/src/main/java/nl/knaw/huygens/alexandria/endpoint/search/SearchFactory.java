package nl.knaw.huygens.alexandria.endpoint.search;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.knaw.huygens.alexandria.api.model.search.SearchInfo;
import nl.knaw.huygens.alexandria.api.model.search.SearchResultPage;

@Singleton
public class SearchFactory {

  @Inject
  public SearchFactory() {
  }

  public SearchResultPage createSearchResultPage(final String baseURI, final int pageNumber, final SearchResult searchResult, final List<Map<String, Object>> results) {
    final int pageSize = searchResult.getPageSize();
    final int lastPageNumber = Math.max(1, searchResult.getTotalPages());

    final SearchInfo searchInfo = new SearchInfo()//
        .setId(searchResult.getId())//
        .setQuery(searchResult.getQuery())//
        .setSearchDurationInMs(searchResult.getSearchDurationInMs())//
        .setTotalPages(searchResult.getTotalPages())//
        .setPageSize(pageSize)//
        .setTotalResults(searchResult.getTotalResults());

    final AtomicInteger counter = new AtomicInteger(pageSize * (pageNumber - 1));
    final List<Map<String, Object>> records = results.stream().map(r -> {
      r.put("_resultNumber", counter.incrementAndGet());
      return r;
    }).collect(toList());

    final SearchResultPage searchResultPage = new SearchResultPage()//
        .setPageNumber(pageNumber)//
        .setSearchInfo(searchInfo)//
        .setRecords(records)//
        .setFirstPageURI(URI.create(baseURI + 1))//
        .setLastPageURI(URI.create(baseURI + (lastPageNumber)));

    if (pageNumber > 1) {
      searchResultPage.setPreviousPageURI(URI.create(baseURI + (pageNumber - 1)));
    }

    final boolean isLast = pageNumber == lastPageNumber;
    if (!isLast) {
      searchResultPage.setNextPageURI(URI.create(baseURI + (pageNumber + 1)));
    }

    return searchResultPage;
  }

}
