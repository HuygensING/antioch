package nl.knaw.huygens.alexandria.service;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.storage.Storage;

public class Neo4JService extends TinkerPopService {

  @Inject
  public Neo4JService(AlexandriaConfiguration config, LocationBuilder locationBuilder) {
    super(getStorage(config), locationBuilder);
  }

  private static Storage getStorage(AlexandriaConfiguration config) {
    Neo4jGraph graph = Neo4jGraph.open(config.getStorageDirectory()+"/neo4jdb");
    return new Storage(graph);
  }

}
