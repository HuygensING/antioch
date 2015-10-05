package nl.knaw.huygens.alexandria.service;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

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

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.VertexLabel;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.graphdb.database.management.ManagementSystem;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.storage.Storage;

@Singleton
public class TitanService extends TinkerPopService {
  private static final String PROP_UUID = "uuid";
  private static final String IDX_RESOURCE_UUID = "resourceByUUID";
  private static final String IDX_ANNOTATION_UUID = "annotationByUUID";
  private static final String IDX_ANNOTATIONBODY_UUID = "annotationBodyByUUID";
  private static TitanGraph titanGraph;

  @Inject
  public TitanService(LocationBuilder locationBuilder, AlexandriaConfiguration configuration) {
    super(getStorage(configuration), locationBuilder);
  }

  @Override
  public Map<String, Object> getMetadata() {
    TitanManagement mgmt = titanGraph.openManagement();
    Map<String, Object> metadata = super.getMetadata();
    Map<String, Object> storageMap = (Map<String, Object>) metadata.get("storage");
    storageMap.put("vertexIndexes", indexNames(mgmt, Vertex.class));
    storageMap.put("edgeIndexes", indexNames(mgmt, Edge.class));
    return metadata;
  }

  private List<String> indexNames(TitanManagement mgmt, Class<? extends Element> elementClass) {
    return StreamSupport.stream(mgmt.getGraphIndexes(elementClass).spliterator(), false)//
        .map(TitanGraphIndex::name)//
        .collect(toList());
  }

  private static Storage getStorage(AlexandriaConfiguration configuration) {
    titanGraph = TitanFactory.open(configuration.getStorageDirectory() + "/titan.properties");
    setIndexes();
    return new Storage(titanGraph);
  }

  private static void setIndexes() {
    createIndexWhenAbsent(IDX_RESOURCE_UUID, "Resource");
    createIndexWhenAbsent(IDX_ANNOTATION_UUID, "Annotation");
    createIndexWhenAbsent(IDX_ANNOTATIONBODY_UUID, "AnnotationBody");
  }

  private static void createIndexWhenAbsent(String uuidIdx, String label) {
    TitanManagement mgmt = titanGraph.openManagement();
    if (!mgmt.containsGraphIndex(uuidIdx)) {
      PropertyKey uuidKey = mgmt.containsPropertyKey(PROP_UUID)//
          ? mgmt.getPropertyKey(PROP_UUID)//
          : mgmt.makePropertyKey(PROP_UUID).dataType(String.class).make();
      VertexLabel vertexLabel = mgmt.containsVertexLabel(label)//
          ? mgmt.getVertexLabel(label)//
          : mgmt.makeVertexLabel(label).make();
      Log.info("creating index '{}' for label '{}' + property '{}'", uuidIdx, label, PROP_UUID);
      mgmt.buildIndex(uuidIdx, Vertex.class)//
          .addKey(uuidKey)//
          .indexOnly(vertexLabel)//
          .unique()//
          .buildCompositeIndex();
      mgmt.commit();

      try {
        ManagementSystem.awaitGraphIndexStatus(titanGraph, uuidIdx).call();
      } catch (InterruptedException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }

      titanGraph.tx().commit();
    }
  }
}
