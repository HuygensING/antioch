package nl.knaw.huygens.alexandria.service;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.storage.Storage;

/*
 * #%L
 * alexandria-service-tinkerpop-neo4j
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

@Singleton
class Neo4JService extends TinkerPopService {

  private static Neo4jGraph graph;

  @Inject
  public Neo4JService(AlexandriaConfiguration config, LocationBuilder locationBuilder) {
    super(getStorage(config), locationBuilder);
  }

  private static Storage getStorage(AlexandriaConfiguration config) {
    String dataDir = config.getStorageDirectory() + "/neo4jdb";
    createWhenAbsent(dataDir);
    try {
      graph = Neo4jGraph.open(dataDir);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("There was an error starting up the Neo4J graph; If this was caused by an interrupted indexing job, restarting the server might help.");
    }
    setIndexes();
    return new Storage(graph);
  }

  private static void setIndexes() {
    System.out.print("updating 7 indexes: .");
    setUniqueIndex("Resource", Storage.IDENTIFIER_PROPERTY);
    System.out.print(".");
    setUniqueIndex("Annotation", Storage.IDENTIFIER_PROPERTY);
    System.out.print(".");
    setUniqueIndex("AnnotationBody", Storage.IDENTIFIER_PROPERTY);
    System.out.print(".");
    // setUniqueIndex(VertexLabels.TEXTRANGEANNOTATION, Storage.IDENTIFIER_PROPERTY);
    // System.out.print(".");
    runCypher("create index on :Resource(cargo)");
    System.out.print(".");
    runCypher("create index on :Annotation(who)");
    System.out.print(".");
    runCypher("create index on :AnnotationBody(type)");
    graph.tx().commit();
    System.out.println(" indexing started!");
    System.out.println("It might take a while to finish, but you can use the server right away.");
  }

  private static void setUniqueIndex(String label, String property) {
    runCypher("create constraint on (r:" + label + ") assert r." + property + " is unique");
    runCypher("create index on :" + label + "(state)");
  }

  private static void runCypher(String cypher) {
    GraphTraversal<Object, Object> traversal = graph.cypher(cypher);
    if (traversal.hasNext()) {
      Object next = traversal.next();
      // Log.info("next={}", next);
    }
  }

  private static void createWhenAbsent(String dataDir) {
    File file = new File(dataDir);
    if (!file.isDirectory()) {
      file.mkdirs();
    }
  }

}
