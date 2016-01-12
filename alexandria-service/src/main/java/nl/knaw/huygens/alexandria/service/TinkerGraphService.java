package nl.knaw.huygens.alexandria.service;

/*
 * #%L
 * alexandria-service
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

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.storage.Storage;

@Singleton
public class TinkerGraphService extends TinkerPopService {
  protected static final Storage STORAGE = getStorage();

  @Inject
  public TinkerGraphService(LocationBuilder locationBuilder) {
    super(STORAGE, locationBuilder);
  }

  private static Storage getStorage() {
    TinkerGraph tg = TinkerGraph.open();
    tg.createIndex(Storage.IDENTIFIER_PROPERTY, Vertex.class);
    return new Storage(tg);
  }
}
