package nl.knaw.huygens.alexandria.storage;

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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.storage.frames.IdentifiableVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;
import nl.knaw.huygens.alexandria.storage.frames.VF;
import peapod.FramedGraph;
import peapod.FramedGraphTraversal;

@Singleton
public class Storage {
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
      rollbackTransaction();
      throw e;
    }
  }

  Boolean getTransactionIsOpen() {
    return getTransactionOpen().get();
  }

  private ThreadLocal<Boolean> getTransactionOpen() {
    if (transactionOpen == null) {
      transactionOpen = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
          return false;
        }
      };
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
      rollbackTransaction();
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
      Log.warn("restoring from " + format.name() + " may lead to duplicate id errors, use gryo if possible");
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

  public void saveToDisk(final String file) {
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
      Log.error("rollback called, but transactions are not supported by graph {}", graph);
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
