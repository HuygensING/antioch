package nl.knaw.huygens.alexandria.storage;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;

@Singleton
public class Neo4JStorage extends Storage {
  @Inject
  public Neo4JStorage(AlexandriaConfiguration configuration) {
    super(Neo4jGraph.open(configuration.getStorageDirectory()));
  }
}
