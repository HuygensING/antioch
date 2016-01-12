package nl.knaw.huygens.alexandria.service;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;

/*
 * #%L
 * alexandria-service-tinkerpop-titan
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

import org.apache.commons.lang3.text.WordUtils;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.google.common.collect.Lists;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.VertexLabel;
import com.thinkaurelius.titan.core.schema.ConsistencyModifier;
import com.thinkaurelius.titan.core.schema.SchemaAction;
import com.thinkaurelius.titan.core.schema.SchemaStatus;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.schema.TitanManagement.IndexBuilder;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.thinkaurelius.titan.graphdb.database.management.GraphIndexStatusReport;
import com.thinkaurelius.titan.graphdb.database.management.ManagementSystem;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.storage.Storage;

@Singleton
public class TitanService extends TinkerPopService {
  private static final boolean UNIQUE = true;
  private static final String PROP_TYPE = "type";
  private static final String PROP_WHO = "who";
  private static final String PROP_STATE = "state";

  enum VertexCompositeIndex {
    IDX_ANY_STATE(null, PROP_STATE, !UNIQUE), //
    IDX_RESOURCE_UUID("Resource", Storage.IDENTIFIER_PROPERTY, UNIQUE), //
    IDX_ANNOTATION_UUID("Annotation", Storage.IDENTIFIER_PROPERTY, UNIQUE), //
    IDX_ANNOTATION_WHO("Annotation", PROP_WHO, !UNIQUE), //
    IDX_ANNOTATIONBODY_UUID("AnnotationBody", Storage.IDENTIFIER_PROPERTY, UNIQUE), //
    IDX_ANNOTATIONBODY_TYPE("AnnotationBody", PROP_TYPE, !UNIQUE);

    public String label;
    public String property;
    public boolean unique;
    public String name;

    VertexCompositeIndex(String label, String property, boolean unique) {
      this.label = label;
      this.property = property;
      this.unique = unique;
      this.name = (label == null ? "" : label) + "By" + WordUtils.capitalize(property);
    }

  }

  private static TitanGraph titanGraph;
  private AlexandriaConfiguration configuration;

  static class IndexInfo {
    private String name;
    private String backingIndex;
    private Class<? extends Element> indexedElement;
    private SchemaStatus indexStatus;

    public IndexInfo(TitanGraphIndex index) {
      name = index.name();
      backingIndex = index.getBackingIndex();
      indexedElement = index.getIndexedElement();
      PropertyKey[] fieldKeys = index.getFieldKeys();
      indexStatus = index.getIndexStatus(fieldKeys[0]);
    }

    public String getBackingIndex() {
      return backingIndex;
    }

    public Class<? extends Element> getIndexedElement() {
      return indexedElement;
    }

    public SchemaStatus getIndexStatus() {
      return indexStatus;
    }

    public String getName() {
      return name;
    }

  }

  @Inject
  public TitanService(LocationBuilder locationBuilder, AlexandriaConfiguration configuration) {
    super(getStorage(configuration), locationBuilder);
    this.configuration = configuration;
  }

  @Override
  public Map<String, Object> getMetadata() {
    TitanManagement mgmt = titanGraph.openManagement();
    Map<String, Object> metadata = super.getMetadata();
    @SuppressWarnings("unchecked")
    Map<String, Object> storageMap = (Map<String, Object>) metadata.get("storage");
    storageMap.put("vertexIndexes", indexInfo(mgmt, Vertex.class));
    storageMap.put("edgeIndexes", indexInfo(mgmt, Edge.class));
    return metadata;
  }

  private static Storage getStorage(AlexandriaConfiguration configuration) {
    titanGraph = TitanFactory.open(configuration.getStorageDirectory() + "/titan.properties");
    setIndexes();
    return new Storage(titanGraph);
  }

  private List<IndexInfo> indexInfo(TitanManagement mgmt, Class<? extends Element> elementClass) {
    return StreamSupport.stream(mgmt.getGraphIndexes(elementClass).spliterator(), false)//
        .map(IndexInfo::new)//
        .collect(toList());
  }

  private static void setIndexes() {
    List<String> reindex = Lists.newArrayList();
    TitanManagement mgmt = titanGraph.openManagement();
    for (VertexCompositeIndex compositeIndex : VertexCompositeIndex.values()) {
      boolean created = createIndexWhenAbsent(mgmt, compositeIndex);
      if (created) {
        reindex.add(compositeIndex.name);
      }
    }
    Log.info("saving indexes");
    mgmt.commit();

    mgmt = titanGraph.openManagement();
    Log.info("wait for completion");
    for (VertexCompositeIndex compositeIndex : VertexCompositeIndex.values()) {
      waitForCompletion(mgmt, compositeIndex);
    }
    mgmt.commit();

    mgmt = titanGraph.openManagement();
    for (String newIndex : reindex) {
      Log.info("reindexing {}", newIndex);
      try {
        mgmt.updateIndex(mgmt.getGraphIndex(newIndex), SchemaAction.REINDEX).get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
    mgmt.commit();

    titanGraph.tx().rollback();
  }

  private static boolean createIndexWhenAbsent(TitanManagement mgmt, VertexCompositeIndex compositeIndex) {
    String name = compositeIndex.name;
    if (!mgmt.containsGraphIndex(name)) {
      String property = compositeIndex.property;
      String label = compositeIndex.label;
      boolean unique = compositeIndex.unique;
      Log.info("building {} index '{}' for label '{}' + property '{}'", unique ? "unique" : "non-unique", name, label, property);

      PropertyKey uuidKey = mgmt.containsPropertyKey(property)//
          ? mgmt.getPropertyKey(property)//
          : mgmt.makePropertyKey(property).dataType(String.class).make();

      IndexBuilder indexBuilder = mgmt.buildIndex(name, Vertex.class)//
          .addKey(uuidKey);

      if (label != null) {
        VertexLabel vertexLabel = mgmt.containsVertexLabel(label)//
            ? mgmt.getVertexLabel(label)//
            : mgmt.makeVertexLabel(label).make();
        indexBuilder = indexBuilder.indexOnly(vertexLabel);
      }

      if (unique) {
        indexBuilder = indexBuilder.unique();
      }

      TitanGraphIndex index = indexBuilder.buildCompositeIndex();
      mgmt.setConsistency(index, ConsistencyModifier.LOCK);

      return true;
    }
    return false;
  }

  private static void waitForCompletion(TitanManagement mgmt, VertexCompositeIndex compositeIndex) {
    String name = compositeIndex.name;
    TitanGraphIndex graphIndex = mgmt.getGraphIndex(name);
    PropertyKey propertyKey = graphIndex.getFieldKeys()[0];
    // For composite indexes, the propertyKey is ignored and the status of the index as a whole is returned
    if (!SchemaStatus.ENABLED.equals(graphIndex.getIndexStatus(propertyKey))) {
      try {
        GraphIndexStatusReport report = ManagementSystem.awaitGraphIndexStatus(titanGraph, name).call();
        Log.info("report={}", report);
      } catch (InterruptedException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void destroy() {
    // Log.info("destroy called");
    super.destroy();
    if (titanGraph.isOpen()) {
      Log.info("closing {}:", titanGraph);
      titanGraph.close();
    }
    // Log.info("destroy finished");
  }

  @Override
  Storage clearGraph() {
    titanGraph.close();
    TitanCleanup.clear(titanGraph);
    return getStorage(configuration);
  }
}
