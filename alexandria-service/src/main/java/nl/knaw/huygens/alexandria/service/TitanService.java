package nl.knaw.huygens.alexandria.service;

/*
 * #%L
 * alexandria-service
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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

import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.graphdb.database.management.ManagementSystem;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.storage.Storage;

@Singleton
public class TitanService extends TinkerPopService {
  private static final String PROP_UUID = "uuid";
  private static final String IDX_UUID = "byUUID";

  @Inject
  public TitanService(LocationBuilder locationBuilder, AlexandriaConfiguration configuration) {
    super(getStorage(configuration), locationBuilder);
  }

  private static Storage getStorage(AlexandriaConfiguration configuration) {
    TitanGraph tg = TitanFactory.open(configuration.getStorageDirectory() + "/titan.properties");
    setIndexes(tg);
    return new Storage(tg);
  }

  private static void setIndexes(TitanGraph tg) {
    TitanManagement tm = tg.openManagement();
    if (!tm.containsGraphIndex(IDX_UUID)) {
      PropertyKey uuidKey = tm.containsPropertyKey(PROP_UUID)//
          ? tm.getPropertyKey(PROP_UUID)//
          : tm.makePropertyKey(PROP_UUID).dataType(String.class).make();
      Log.info("creating index '{}' for property '{}'", IDX_UUID, PROP_UUID);
      tm.buildIndex(IDX_UUID, Vertex.class).addKey(uuidKey).unique().buildCompositeIndex();
      tm.commit();
      tg.tx().commit();

      try {
        ManagementSystem.awaitGraphIndexStatus(tg, IDX_UUID).call();
      } catch (InterruptedException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }

    // try {
    // tm.updateIndex(tm.getGraphIndex(IDX_UUID), SchemaAction.REINDEX).get();
    // } catch (InterruptedException | ExecutionException e) {
    // e.printStackTrace();
    // throw new RuntimeException(e);
    // }
    tm.commit();
  }
}
