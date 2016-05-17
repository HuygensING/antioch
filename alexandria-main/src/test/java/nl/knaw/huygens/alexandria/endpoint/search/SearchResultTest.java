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
