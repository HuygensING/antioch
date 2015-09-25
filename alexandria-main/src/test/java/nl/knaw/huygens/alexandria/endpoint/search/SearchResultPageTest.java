package nl.knaw.huygens.alexandria.endpoint.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import nl.knaw.huygens.Log;

public class SearchResultPageTest {
  String baseURI = "http://example.org/search/1/page/";
  static Map<String, Object> map = Maps.newHashMap();

  static {
    map.put("what", "ever");
  }

  List<Map<String, Object>> ten_results = ImmutableList.<Map<String, Object>> builder()//
      .add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map))//
      .add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map))//
      .add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map))//
      .add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map))//
      .add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map))//
      .build();

  List<Map<String, Object>> five_results = ImmutableList.<Map<String, Object>> builder()//
      .add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map))//
      .add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map))//
      .add(new HashMap<String, Object>(map))//
      .build();

  @Test
  public void testFirstPageOfSeveral() {
    SearchResultPage srp = new SearchResultPage(baseURI, 1, false, 10);
    srp.setResults(ten_results);
    assertThat(srp.getPreviousPage()).isNull();
    assertThat(srp.getNextPage()).hasToString(baseURI + 2);
    List<Map<String, Object>> records = srp.getRecords();
    Log.info("records={}", records);
    assertThat(records).hasSize(10);
    assertThat(records.get(0)).containsEntry("what", "ever");
    assertThat(records.get(0)).containsEntry("_resultNumber", 1);
  }

  @Test
  public void testSecondPageOfThree() {
    SearchResultPage srp = new SearchResultPage(baseURI, 2, false, 10);
    srp.setResults(ten_results);
    assertThat(srp.getPreviousPage()).hasToString(baseURI + 1);
    assertThat(srp.getNextPage()).hasToString(baseURI + 3);
    List<Map<String, Object>> records = srp.getRecords();
    Log.info("records={}", records);
    assertThat(records).hasSize(10);
    assertThat(records.get(0)).containsEntry("what", "ever");
    assertThat(records.get(0)).containsEntry("_resultNumber", 11);
  }

  @Test
  public void testLastPageOfSeveral() {
    SearchResultPage srp = new SearchResultPage(baseURI, 2, true, 10);
    srp.setResults(five_results);
    assertThat(srp.getPreviousPage()).hasToString(baseURI + 1);
    assertThat(srp.getNextPage()).isNull();
    List<Map<String, Object>> records = srp.getRecords();
    Log.info("records={}", records);
    assertThat(records).hasSize(5);
    assertThat(records.get(0)).containsEntry("what", "ever");
    assertThat(records.get(0)).containsEntry("_resultNumber", 11);
    assertThat(records.get(4)).containsEntry("_resultNumber", 15);
  }

}
