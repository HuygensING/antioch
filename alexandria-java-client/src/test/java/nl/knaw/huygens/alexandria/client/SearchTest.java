package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.SearchResultPage;

public class SearchTest extends AlexandriaClientTest {
  private UUID resource1;
  private UUID resource2;
  private UUID annotation1;
  private UUID annotation2;

  @Before
  public void before() {
    client.setAuthKey(AUTHKEY);
    client.setAutoConfirm(true);

    resource1 = createResource("Resource 1");
    resource2 = createResource("Resource 2");
    annotation1 = annotateResource(resource1, "type 1", "value 1");
    annotation2 = annotateResource(resource2, "type 2", "value 2");
  }

  @Test
  public void testAddSearchWorks() {
    AlexandriaQuery query = new AlexandriaQuery()// t
        .setFind("annotation")//
        .setWhere("type:eq(\"type 1\")")//
        .setReturns("id, resource.id");
    RestResult<UUID> result = client.addSearch(query);
    assertRequestSucceeded(result);

    UUID searchId = result.get();
    RestResult<SearchResultPage> pageResult = client.getSearchResultPage(searchId);
    assertRequestSucceeded(pageResult);
    Log.info("search took {} ms", pageResult.getTurnaroundTime().toMillis());
    SearchResultPage searchResultPage = pageResult.get();
    List<Map<String, Object>> records = searchResultPage.getRecords();
    assertThat(records).hasSize(1);
    Map<String, Object> firstRecord = records.get(0);
    assertThat(firstRecord).containsEntry("id", annotation1.toString());
    assertThat(firstRecord).containsEntry("resource.id", resource1.toString());
  }

}
