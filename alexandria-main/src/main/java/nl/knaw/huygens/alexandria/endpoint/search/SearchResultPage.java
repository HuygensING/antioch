package nl.knaw.huygens.alexandria.endpoint.search;

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

import nl.knaw.huygens.alexandria.endpoint.JsonWrapperObject;
import nl.knaw.huygens.alexandria.endpoint.resource.PropertyPrefix;

@JsonInclude(Include.NON_NULL)
@JsonTypeName("searchresultPage")
public class SearchResultPage extends JsonWrapperObject {
  private int pageNum;
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

  public SearchResultPage(String baseURI, int pageNum, boolean isLast) {
    this.baseURI = baseURI;
    this.pageNum = pageNum;
    this.isLast = isLast;
    this.counter = new AtomicInteger((int) SearchResult.PAGESIZE * (pageNum - 1));
    this.counterFunction = t -> counter.incrementAndGet();
  }

  @JsonProperty(PropertyPrefix.LINK + "previousPage")
  public URI getPreviousPage() {
    return pageNum > 1 ? URI.create(baseURI + (pageNum - 1)) : null;
  }

  @JsonProperty(PropertyPrefix.LINK + "nextPage")
  public URI getNextPage() {
    return isLast ? null : URI.create(baseURI + (pageNum + 1));
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
