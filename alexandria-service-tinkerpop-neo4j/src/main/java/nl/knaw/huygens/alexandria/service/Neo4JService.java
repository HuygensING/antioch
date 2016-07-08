package nl.knaw.huygens.alexandria.service;

import java.io.File;

/*
 * #%L
 * alexandria-service-tinkerpop-neo4j
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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.storage.Storage;

@Singleton
public class Neo4JService extends TinkerPopService {

  @Inject
  public Neo4JService(AlexandriaConfiguration config, LocationBuilder locationBuilder) {
    super(getStorage(config), locationBuilder);
  }

  private static Storage getStorage(AlexandriaConfiguration config) {
    String dataDir = config.getStorageDirectory() + "/neo4jdb";
    createWhenAbsent(dataDir);
    Neo4jGraph graph = Neo4jGraph.open(dataDir);
    return new Storage(graph);
  }

  private static void createWhenAbsent(String dataDir) {
    File file = new File(dataDir);
    if (!file.isDirectory()) {
      file.mkdirs();
    }
  }

}
