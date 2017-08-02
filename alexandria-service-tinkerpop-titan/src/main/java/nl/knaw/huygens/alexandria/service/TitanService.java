package nl.knaw.huygens.alexandria.service;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/*
 * #%L
 * alexandria-service-tinkerpop-titan
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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.storage.Storage;
import nl.knaw.huygens.alexandria.util.StreamUtil;

@Singleton
public class TitanService extends TinkerPopService {
  private static final Logger LOG = LoggerFactory.getLogger(TitanService.class);
  private static final boolean UNIQUE = true;
  private static final String PROP_TYPE = "type";
  private static final String PROP_WHO = "who";
  private static final String PROP_STATE = "state";

  enum VertexCompositeIndex {
    IDX_ANY_STATE(null, PROP_STATE, !UNIQUE), //
    IDX_RESOURCE_UUID("Resource", Storage.IDENTIFIER_PROPERTY, UNIQUE), //
    // IDX_RESOURCE_CARGO("Resource", PROP_CARGO, !UNIQUE), //
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
    titanGraph = TitanFactory.open(String.join(":", "berkeleyje", configuration.getStorageDirectory()));
    setIndexes();
    return new Storage(titanGraph);
  }

  private List<IndexInfo> indexInfo(TitanManagement mgmt, Class<? extends Element> elementClass) {
    return StreamUtil.stream(mgmt.getGraphIndexes(elementClass))//
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
    LOG.info("saving indexes");
    mgmt.commit();

    mgmt = titanGraph.openManagement();
    LOG.info("wait for completion");
    for (VertexCompositeIndex compositeIndex : VertexCompositeIndex.values()) {
      waitForCompletion(mgmt, compositeIndex);
    }
    mgmt.commit();

    mgmt = titanGraph.openManagement();
    for (String newIndex : reindex) {
      LOG.info("reindexing {}", newIndex);
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
      LOG.info("building {} index '{}' for label '{}' + property '{}'", unique ? "unique" : "non-unique", name, label, property);

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
        LOG.info("report={}", report);
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
      LOG.info("closing {}:", titanGraph);
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
