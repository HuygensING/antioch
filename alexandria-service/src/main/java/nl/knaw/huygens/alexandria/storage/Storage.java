package nl.knaw.huygens.alexandria.storage;

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

import static org.apache.tinkerpop.gremlin.process.traversal.P.lt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Graph.Features.GraphFeatures;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLIo;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONIo;

import com.google.common.collect.Maps;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;
import peapod.FramedGraph;
import peapod.FramedGraphTraversal;

public class Storage {
  public static final String IDENTIFIER_PROPERTY = "uuid";

  private Graph graph;
  private FramedGraph framedGraph;

  private String dumpfile;
  private boolean supportsTransactions;
  private boolean supportsPersistence;

  public Storage(final Graph graph) {
    setGraph(graph);
  }

  // - public methods - //

  // This is for the acceptancetest's benefit, so the tests kan start with a clear graph
  public void setGraph(final Graph graph) {
    this.graph = graph;
    this.framedGraph = new FramedGraph(graph, ResourceVF.class.getPackage());
    final GraphFeatures graphFeatures = graph.features().graph();
    this.supportsPersistence = graphFeatures.supportsPersistence();
    this.supportsTransactions = graphFeatures.supportsTransactions();
  }

  public boolean supportsTransactions() {
    return supportsTransactions;
  }

  public boolean supportsPersistence() {
    return supportsPersistence;
  }

  // framedGraph methods

  public void startTransaction() {
    if (supportsTransactions()) {
      framedGraph.tx().rollback();
    }
  }

  public void commitTransaction() {
    if (supportsTransactions()) {
      framedGraph.tx().commit();
      // framedGraph.tx().close();
    }
    if (!supportsPersistence()) {
      saveToDisk(getDumpFile());
    }
  }

  public void rollbackTransaction() {
    if (supportsTransactions()) {
      framedGraph.tx().rollback();
      // framedGraph.tx().close();
    } else {
      Log.error("rollback called, but transactions are not supported by graph {}", graph);
    }
  }

  public boolean existsVF(final Class<? extends AlexandriaVF> vfClass, final UUID uuid) {
    assertClass(vfClass);
    return find(vfClass, uuid).tryNext().isPresent();
  }

  public <A extends AlexandriaVF> A createVF(final Class<A> vfClass) {
    assertClass(vfClass);
    return framedGraph.addVertex(vfClass);
  }

  public <A extends AlexandriaVF> Optional<A> readVF(final Class<A> vfClass, final UUID uuid) {
    assertClass(vfClass);
    return firstOrEmpty(find(vfClass, uuid).toList());
  }

  public <A extends AlexandriaVF> Optional<A> readVF(final Class<A> vfClass, final UUID uuid, final Integer revision) {
    assertClass(vfClass);
    return firstOrEmpty(find(vfClass, uuid, revision).toList());
  }

  public <A extends AlexandriaVF> FramedGraphTraversal<Object, A> find(Class<A> vfClass) {
    return framedGraph.V(vfClass);
  }

  public GraphTraversal<Vertex, Vertex> getVertexTraversal(Object... vertexIds) {
    return graph.traversal().V(vertexIds);
  }

  // graph methods

  public void removeExpiredTentatives(final Long threshold) {
    getVertexTraversal()//
        .has("state", AlexandriaState.TENTATIVE.name())//
        .has("stateSince", lt(threshold))//
        .forEachRemaining(Element::remove);
  }

  public void removeVertexWithId(final String annotationBodyId) {
    getVertexTraversal()//
        .has(IDENTIFIER_PROPERTY, annotationBodyId).next().remove();
  }

  public void dumpToGraphSON(final OutputStream os) throws IOException {
    graph.io(new GraphSONIo.Builder()).writer().create().writeGraph(os, graph);
  }

  public void dumpToGraphML(final OutputStream os) throws IOException {
    graph.io(new GraphMLIo.Builder()).writer().create().writeGraph(os, graph);
  }

  public void readGraph(DumpFormat format, String filename) throws IOException {
    graph.io(format.builder).readGraph(filename);
  }

  public void writeGraph(DumpFormat format, String filename) throws IOException {
    graph.io(format.builder).writeGraph(filename);
  }

  public void loadFromDisk(final String file) {
    try {
      graph.io(IoCore.graphml()).readGraph(file);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void saveToDisk(final String file) {
    if (file != null) {
      try {
        graph.io(IoCore.graphml()).writeGraph(file);
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void setDumpFile(final String dumpfile) {
    this.dumpfile = dumpfile;
  }

  // - private methods - //

  private <A extends AlexandriaVF> FramedGraphTraversal<Object, A> find(final Class<A> vfClass, final UUID uuid) {
    return find(vfClass).has(IDENTIFIER_PROPERTY, uuid.toString());
  }

  private <A extends AlexandriaVF> FramedGraphTraversal<Object, A> find(final Class<A> vfClass, final UUID uuid, final Integer revision) {
    return find(vfClass).has(IDENTIFIER_PROPERTY, uuid.toString() + "." + revision);
  }

  private String getDumpFile() {
    return dumpfile;
  }

  private <A> Optional<A> firstOrEmpty(final List<A> results) {
    return results.isEmpty() ? Optional.empty() : Optional.ofNullable(results.get(0));
  }

  private void assertClass(final Class<? extends AlexandriaVF> clazz) {
    if (clazz.getAnnotationsByType(peapod.annotations.Vertex.class).length == 0) {
      throw new RuntimeException("Class " + clazz + " has no peapod @Vertex annotation, are you sure it is the correct class?");
    }
  }

  public Map<String, Object> getMetadata() {
    Map<String, Object> metadata = Maps.newLinkedHashMap();
    metadata.put("features", graph.features().toString().split(System.lineSeparator()));
    GraphTraversalSource traversal = graph.traversal();
    metadata.put("vertices", count(traversal.V()));
    metadata.put("edges", count(traversal.E()));
    return metadata;
  }

  private Long count(GraphTraversal<?, ?> graphTraversal) {
    return graphTraversal.count().next();
  }

  public void destroy() {
    // Log.info("destroy called");
    try {
      Log.info("closing graph {}", graph);
      graph.close();
      Log.info("graph closed: {}", graph);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    // Log.info("destroy done");
  }

}
