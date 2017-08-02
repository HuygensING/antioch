package nl.knaw.huygens.alexandria.endpoint.search;

/*
 * #%L
 * alexandria-main
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;

public class SearchResultTest {
  @Test
  public void testTotalPages() {
    AlexandriaConfiguration config = new MockConfiguration();
    SearchResult sr = new SearchResult(new LocationBuilder(config, new EndpointPathResolver()));
    sr.setQuery(new AlexandriaQuery().setPageSize(10));
    Map<String, Object> map = ImmutableMap.of("what", "ever");
    List<Map<String, Object>> results = Collections.nCopies(11, map);
    sr.setResults(results);
    UUID id = UUID.randomUUID();
    sr.setId(id);
    assertThat(sr.getTotalResults()).isEqualTo(11);
    assertThat(sr.getTotalPages()).isEqualTo(2);
    String expected = config.getBaseURI() + EndpointPaths.SEARCHES + "/" + id + "/" + EndpointPaths.RESULTPAGES + "/1";
    assertThat(sr.getFirstResultPage()).hasToString(expected);
  }

}
