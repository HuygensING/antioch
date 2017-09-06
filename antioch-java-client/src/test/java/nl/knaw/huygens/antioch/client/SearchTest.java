package nl.knaw.huygens.antioch.client;

/*
 * #%L
 * antioch-java-client
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.antioch.api.model.search.AntiochQuery;
import nl.knaw.huygens.antioch.api.model.search.SearchResultPage;

public class SearchTest extends AntiochClientTest {
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
    AntiochQuery query = new AntiochQuery()// t
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
