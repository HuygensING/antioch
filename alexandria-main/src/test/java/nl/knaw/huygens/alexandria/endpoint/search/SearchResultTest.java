package nl.knaw.huygens.alexandria.endpoint.search;

import static org.assertj.core.api.StrictAssertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.config.MockConfiguration;
import nl.knaw.huygens.alexandria.endpoint.EndpointPathResolver;
import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;

public class SearchResultTest {
  @Test
  public void testTotalPages() {
    AlexandriaConfiguration config = new MockConfiguration();
    SearchResult sr = new SearchResult(new LocationBuilder(config, new EndpointPathResolver()));
    sr.setQuery(new AlexandriaQuery().setPageSize(10));
    Map<String, Object> map = ImmutableMap.of("what", "ever");
    List<Map<String, Object>> results = ImmutableList.<Map<String, Object>> builder()//
        .add(map).add(map).add(map).add(map).add(map)//
        .add(map).add(map).add(map).add(map).add(map)//
        .add(map)//
        .build();
    sr.setResults(results);
    UUID id = UUID.randomUUID();
    sr.setId(id);
    assertThat(sr.getTotalResults()).isEqualTo(11);
    assertThat(sr.getTotalResultPages()).isEqualTo(2);
    assertThat(sr.getFirstResultPage()).hasToString(config.getBaseURI() + EndpointPaths.SEARCHES + "/" + id + "/resultPages/1");
  }

}
