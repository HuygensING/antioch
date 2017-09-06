package nl.knaw.huygens.antioch.endpoint.search;

/*
 * #%L
 * antioch-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.antioch.api.model.search.AntiochQuery;
import nl.knaw.huygens.antioch.api.model.search.SearchResultPage;

public class SearchResultPageTest {
  private static final ImmutableMap<String, String> SAMPLE_MAP = ImmutableMap.of("what", "ever");

  private final String baseURI = "http://example.org/search/1/page/";

  private final List<Map<String, Object>> five_results = ImmutableList.of(map(), map(), map(), map(), map());

  private final List<Map<String, Object>> ten_results = ImmutableList.of( //
      map(), map(), map(), map(), map(), map(), map(), map(), map(), map());

  private static Map<String, Object> map() {
    return new HashMap<>(SAMPLE_MAP);
  }

  private final SearchFactory searchFactory = new SearchFactory();

  @Test
  public void testFirstPageOfSeveral() {
    SearchResultPage srp = searchFactory.createSearchResultPage(baseURI, 1, mockResult(2, 10), ten_results);
    assertThat(srp.getPreviousPageURI()).isNull();
    assertThat(srp.getNextPageURI()).hasToString(baseURI + 2);
    List<Map<String, Object>> records = srp.getRecords();
    Log.info("records={}", records);
    assertThat(records).hasSize(10);
    assertThat(records.get(0)).containsEntry("what", "ever");
    assertThat(records.get(0)).containsEntry("_resultNumber", 1);
  }

  @Test
  public void testSecondPageOfThree() {
    SearchResultPage srp = searchFactory.createSearchResultPage(baseURI, 2, mockResult(3, 10), ten_results);
    assertThat(srp.getPreviousPageURI()).hasToString(baseURI + 1);
    assertThat(srp.getNextPageURI()).hasToString(baseURI + 3);
    List<Map<String, Object>> records = srp.getRecords();
    Log.info("records={}", records);
    assertThat(records).hasSize(10);
    assertThat(records.get(0)).containsEntry("what", "ever");
    assertThat(records.get(0)).containsEntry("_resultNumber", 11);
  }

  @Test
  public void testLastPageOfSeveral() {
    SearchResultPage srp = searchFactory.createSearchResultPage(baseURI, 2, mockResult(2, 10), five_results);
    assertThat(srp.getPreviousPageURI()).hasToString(baseURI + 1);
    assertThat(srp.getNextPageURI()).isNull();
    List<Map<String, Object>> records = srp.getRecords();
    Log.info("records={}", records);
    assertThat(records).hasSize(5);
    assertThat(records.get(0)).containsEntry("what", "ever");
    assertThat(records.get(0)).containsEntry("_resultNumber", 11);
    assertThat(records.get(4)).containsEntry("_resultNumber", 15);
  }

  private SearchResult mockResult(int totalPages, int pageSize) {
    SearchResult mock = mock(SearchResult.class);
    when(mock.getTotalPages()).thenReturn(totalPages);
    when(mock.getPageSize()).thenReturn(pageSize);
    when(mock.getId()).thenReturn(UUID.randomUUID());
    AntiochQuery mockQuery = mock(AntiochQuery.class);
    when(mock.getQuery()).thenReturn(mockQuery);
    return mock;
  }

}
