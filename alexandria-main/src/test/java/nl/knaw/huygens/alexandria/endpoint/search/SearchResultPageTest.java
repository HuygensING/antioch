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

import static org.assertj.core.api.Assertions.assertThat;

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
      .add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map))//
      .add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map))//
      .build();

  List<Map<String, Object>> five_results = ImmutableList.<Map<String, Object>> builder()//
      .add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map)).add(new HashMap<String, Object>(map))//
      .build();

  @Test
  public void testFirstPageOfSeveral() {
    SearchResultPage srp = new SearchResultPage(baseURI, 1, false);
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
    SearchResultPage srp = new SearchResultPage(baseURI, 2, false);
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
    SearchResultPage srp = new SearchResultPage(baseURI, 2, true);
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
