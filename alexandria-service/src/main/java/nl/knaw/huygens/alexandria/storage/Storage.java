package nl.knaw.huygens.alexandria.storage;

/*
 * #%L
 * alexandria-service
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

import static org.apache.tinkerpop.gremlin.process.traversal.P.lt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import javax.inject.Singleton;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Graph.Features.GraphFeatures;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLIo;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONIo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.storage.frames.IdentifiableVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;
import nl.knaw.huygens.alexandria.storage.frames.VF;
import peapod.FramedGraph;
import peapod.FramedGraphTraversal;

@Singleton
public class Storage {
  private static final Logger LOG = LoggerFactory.getLogger(Storage.class);
  public static final String IDENTIFIER_PROPERTY = "uuid";

  private Graph graph;
  private FramedGraph framedGraph;

  private String dumpfile;
  private boolean supportsTransactions;
  private boolean supportsPersistence;

  private ThreadLocal<Boolean> transactionOpen;

  public Storage(final Graph graph) {
    setGraph(graph);
  }

  // - public methods - //

  // This is for the acceptancetest's benefit, so the tests kan start with a clear graph
  private void setGraph(final Graph graph) {
    this.graph = graph;
    this.framedGraph = new FramedGraph(graph, ResourceVF.class.getPackage());
    final GraphFeatures graphFeatures = graph.features().graph();
    this.supportsPersistence = graphFeatures.supportsPersistence();
    this.supportsTransactions = graphFeatures.supportsTransactions();
  }

  private boolean supportsTransactions() {
    return supportsTransactions;
  }

  public boolean supportsPersistence() {
    return supportsPersistence;
  }

  // framedGraph methods

  public <A> A runInTransaction(Supplier<A> supplier) {
    boolean inOpenTransaction = getTransactionIsOpen();
    if (!inOpenTransaction) {
      startTransaction();
    }
    try {
      A result = supplier.get();
      if (!inOpenTransaction) {
        commitTransaction();
      }
      return result;

    } catch (Exception e) {
      e.printStackTrace();
      if (getTransactionIsOpen()) {
        rollbackTransaction();
      }
      throw e;
    }
  }

  Boolean getTransactionIsOpen() {
    return getTransactionOpen().get();
  }

  private ThreadLocal<Boolean> getTransactionOpen() {
    if (transactionOpen == null) {
      transactionOpen = ThreadLocal.withInitial(() -> false);
    }
    return transactionOpen;
  }

  public void runInTransaction(Runnable runner) {
    boolean startedInOpenTransaction = getTransactionIsOpen();
    if (!startedInOpenTransaction) {
      startTransaction();
    }
    try {
      runner.run();
      if (!startedInOpenTransaction) {
        commitTransaction();
      }

    } catch (Exception e) {
      e.printStackTrace();
      if (getTransactionIsOpen()) {
        rollbackTransaction();
      }
      throw e;
    }
  }

  public boolean existsVF(final Class<? extends VF> vfClass, final UUID uuid) {
    assertInTransaction();
    assertClass(vfClass);
    return find(vfClass, uuid).tryNext().isPresent();
  }

  public <A extends VF> A createVF(final Class<A> vfClass) {
    assertInTransaction();
    assertClass(vfClass);
    return framedGraph.addVertex(vfClass);
  }

  public <A extends VF> Optional<A> readVF(final Class<A> vfClass, final UUID uuid) {
    assertInTransaction();
    assertClass(vfClass);
    return firstOrEmpty(find(vfClass, uuid).toList());
  }

  public <A extends IdentifiableVF> Optional<A> readVF(final Class<A> vfClass, final UUID uuid, final Integer revision) {
    assertInTransaction();
    assertClass(vfClass);
    return firstOrEmpty(find(vfClass, uuid, revision).toList());
  }

  public <A extends VF> FramedGraphTraversal<Object, A> find(Class<A> vfClass) {
    assertInTransaction();
    assertClass(vfClass);
    return framedGraph.V(vfClass);
  }

  public GraphTraversal<Vertex, Vertex> getVertexTraversal(Object... vertexIds) {
    assertInTransaction();
    return graph.traversal().V(vertexIds);
  }

  public GraphTraversal<Vertex, Vertex> getResourceVertexTraversal(Object... vertexIds) {
    return getVertexTraversal(vertexIds).has(T.label, "Resource");
  }

  // graph methods

  public void removeExpiredTentatives(final Long threshold) {
    assertInTransaction();
    getVertexTraversal()//
        .has("state", AlexandriaState.TENTATIVE.name())//
        .has("stateSince", lt(threshold))//
        .forEachRemaining(Element::remove);
  }

  public void removeVertexWithId(final String annotationBodyId) {
    assertInTransaction();
    getVertexTraversal()//
        .has(IDENTIFIER_PROPERTY, annotationBodyId).next().remove();
  }

  public Map<String, Object> getMetadata() {
    assertInTransaction();
    Map<String, Object> metadata = Maps.newLinkedHashMap();
    metadata.put("features", graph.features().toString().split(System.lineSeparator()));
    GraphTraversalSource traversal = graph.traversal();
    metadata.put("vertices", count(traversal.V()));
    metadata.put("edges", count(traversal.E()));
    return metadata;
  }

  public void dumpToGraphSON(final OutputStream os) throws IOException {
    graph.io(GraphSONIo.build()).writer().create().writeGraph(os, graph);
  }

  public void dumpToGraphML(final OutputStream os) throws IOException {
    graph.io(new GraphMLIo.Builder()).writer().create().writeGraph(os, graph);
  }

  public void readGraph(DumpFormat format, String filename) throws IOException {
    nonGryoWarning(format);
    graph.io(format.builder).readGraph(filename);
  }

  public void writeGraph(DumpFormat format, String filename) throws IOException {
    nonGryoWarning(format);
    graph.io(format.builder).writeGraph(filename);
  }

  private void nonGryoWarning(DumpFormat format) {
    if (!DumpFormat.gryo.equals(format)) {
      LOG.warn("restoring from " + format.name() + " may lead to duplicate id errors, use gryo if possible");
    }
  }

  public void loadFromDisk(final String file) {
    try {
      System.out.println("loading db from " + file + "...");
      graph.io(IoCore.gryo()).readGraph(file);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void saveToDisk(final String file) {
    if (file != null) {
      System.out.println("storing db to " + file + "...");
      try {
        graph.io(IoCore.gryo()).writeGraph(file);
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void setDumpFile(final String dumpfile) {
    this.dumpfile = dumpfile;
  }

  public void destroy() {
    // LOG.info("destroy called");
    try {
      // LOG.info("closing graph {}", graph);
      graph.close();
      // LOG.info("graph closed: {}", graph);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    // LOG.info("destroy done");
  }

  public Vertex addVertex(Object... keyValues) {
    assertInTransaction();
    return graph.addVertex(keyValues);
  }

  public <A extends VF> A frameVertex(Vertex v, Class<A> vfClass) {
    return framedGraph.frame(v, vfClass);
  }

  // - private methods - //

  private <A extends VF> FramedGraphTraversal<Object, A> find(final Class<A> vfClass, final UUID uuid) {
    return find(vfClass).has(IDENTIFIER_PROPERTY, uuid.toString());
  }

  private <A extends VF> FramedGraphTraversal<Object, A> find(final Class<A> vfClass, final UUID uuid, final Integer revision) {
    return find(vfClass).has(IDENTIFIER_PROPERTY, uuid.toString() + "." + revision);
  }

  private String getDumpFile() {
    return dumpfile;
  }

  private <A> Optional<A> firstOrEmpty(final List<A> results) {
    return results.isEmpty() ? Optional.empty() : Optional.ofNullable(results.get(0));
  }

  private Long count(GraphTraversal<?, ?> graphTraversal) {
    return graphTraversal.count().next();
  }

  private void startTransaction() {
    assertTransactionIsClosed();
    if (supportsTransactions()) {
      framedGraph.tx().rollback();
    }
    setTransactionIsOpen(true);
  }

  void setTransactionIsOpen(Boolean b) {
    getTransactionOpen().set(b);
  }

  private void commitTransaction() {
    assertTransactionIsOpen();
    if (supportsTransactions()) {
      tryCommitting(10);
      // framedGraph.tx().close();
    }
    if (!supportsPersistence()) {
      saveToDisk(getDumpFile());
    }
    setTransactionIsOpen(false);
  }

  private void tryCommitting(int count) {
    if (count > 1) {
      try {
        framedGraph.tx().commit();
      } catch (Exception e) {
        // wait
        try {
          LOG.error("exception={}", e);
          Thread.sleep(500);
        } catch (InterruptedException ie) {
          ie.printStackTrace();
        }
        // try again
        tryCommitting(count - 1);
      }
    } else {
      framedGraph.tx().commit();
    }
  }

  private void rollbackTransaction() {
    assertTransactionIsOpen();
    if (supportsTransactions()) {
      framedGraph.tx().rollback();
      // framedGraph.tx().close();
    } else {
      LOG.error("rollback called, but transactions are not supported by graph {}", graph);
    }
    setTransactionIsOpen(false);
  }

  private void assertInTransaction() {
    Preconditions.checkState(getTransactionIsOpen(), "We should be in an open transaction at this point, use runInTransaction()!");
  }

  private void assertClass(final Class<? extends VF> clazz) {
    Preconditions.checkState(//
        clazz.getAnnotationsByType(peapod.annotations.Vertex.class).length > 0, //
        "Class " + clazz + " has no peapod @Vertex annotation, are you sure it's the correct class?"//
    );
  }

  private void assertTransactionIsClosed() {
    Preconditions.checkState(!getTransactionIsOpen(), "We're already inside an open transaction!");
  }

  private void assertTransactionIsOpen() {
    Preconditions.checkState(getTransactionIsOpen(), "We're not in an open transaction!");
  }

}
