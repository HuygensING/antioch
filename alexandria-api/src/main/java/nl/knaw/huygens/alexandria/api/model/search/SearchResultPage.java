package nl.knaw.huygens.alexandria.api.model.search;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;

@JsonInclude(Include.NON_NULL)
@JsonTypeName(JsonTypeNames.SEARCHRESULTPAGE)
public class SearchResultPage extends JsonWrapperObject {
  private int pageNumber;
  private List<Map<String, Object>> records;
  private SearchInfo searchInfo;

  @JsonProperty(PropertyPrefix.LINK + "firstPage")
  private URI firstPageURI;

  @JsonProperty(PropertyPrefix.LINK + "previousPage")
  private URI previousPageURI;

  @JsonProperty(PropertyPrefix.LINK + "nextPage")
  private URI nextPageURI;

  @JsonProperty(PropertyPrefix.LINK + "lastPage")
  private URI lastPageURI;

  public SearchResultPage() {
  }

  public SearchResultPage setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
    return this;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public SearchResultPage setRecords(List<Map<String, Object>> records) {
    this.records = records;
    return this;
  }

  public List<Map<String, Object>> getRecords() {
    return records;
  }

  public SearchResultPage setSearchInfo(SearchInfo searchInfo) {
    this.searchInfo = searchInfo;
    return this;
  }

  public SearchInfo getSearchInfo() {
    return searchInfo;
  }

  public SearchResultPage setFirstPageURI(URI firstPageURI) {
    this.firstPageURI = firstPageURI;
    return this;
  }

  public URI getFirstPageURI() {
    return firstPageURI;
  }

  public SearchResultPage setLastPageURI(URI lastPageURI) {
    this.lastPageURI = lastPageURI;
    return this;
  }

  public URI getLastPageURI() {
    return lastPageURI;
  }

  public URI getPreviousPageURI() {
    return previousPageURI;
  }

  public SearchResultPage setPreviousPageURI(URI previousPageURI) {
    this.previousPageURI = previousPageURI;
    return this;
  }

  public URI getNextPageURI() {
    return nextPageURI;
  }

  public SearchResultPage setNextPageURI(URI nextPageURI) {
    this.nextPageURI = nextPageURI;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}
