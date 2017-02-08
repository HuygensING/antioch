package nl.knaw.huygens.alexandria.endpoint.search;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.SearchResultPage;

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
    AlexandriaQuery mockQuery = mock(AlexandriaQuery.class);
    when(mock.getQuery()).thenReturn(mockQuery);
    return mock;
  }

}
