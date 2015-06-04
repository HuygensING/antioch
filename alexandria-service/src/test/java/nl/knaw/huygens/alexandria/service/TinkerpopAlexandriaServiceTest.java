package nl.knaw.huygens.alexandria.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

import com.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import com.tinkerpop.gremlin.structure.Vertex;

public class TinkerpopAlexandriaServiceTest {
  TinkerpopAlexandriaService service = new TinkerpopAlexandriaService();

  @Test
  public void test() {
    Graph g = TinkerGraph.open();
    Log.info("graph features: {}", g.features());
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance("who", Instant.ofEpochSecond(1000000), "why");
    UUID uuid = new UUID(1l, 1l);
    String ref = "ref";
    boolean created = service.createOrUpdateResource(uuid, ref, provenance);
    assertThat(created).isTrue();
  }

  @Test
  public void neo4jtest() throws Exception {
    Neo4jGraph g = Neo4jGraph.open("C:/Users/BramB/My Documents/Neo4j/alexandria.graphdb");
    // Log.info("graph features: {}", g.features());
    Vertex v = g.addVertex(Labels.Resource.toString());
    v.properties("name", "NAME", "id", "NAM001");
    v.property("name").property("when", "now");
    g.io().writeGraphSON("target/graph.json");
    g.close();
  }
}
