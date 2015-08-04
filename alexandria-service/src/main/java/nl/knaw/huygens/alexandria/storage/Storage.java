package nl.knaw.huygens.alexandria.storage;

import static org.apache.tinkerpop.gremlin.process.traversal.P.lt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Graph.Features.GraphFeatures;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLIo;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONIo;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaState;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaVF;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;
import peapod.FramedGraph;
import peapod.FramedGraphTraversal;
import peapod.annotations.Vertex;

public class Storage {
  private static final String IDENTIFIER_PROPERTY = "uuid";

  private Graph graph;
  private FramedGraph framedGraph;

  private Transaction tx;
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
      tx = framedGraph.tx();
    }
  }

  public void commitTransaction() {
    if (supportsTransactions()) {
      tx.commit();
      tx.close();
    }
    if (!supportsPersistence()) {
      saveToDisk(getDumpFile());
    }
  }

  public void rollbackTransaction() {
    if (supportsTransactions()) {
      tx.rollback();
      tx.close();
    } else {
      Log.error("rollback called, but transactions are not supported by graph {}", graph);
    }
  }

  public boolean existsVF(final Class<? extends AlexandriaVF> vfClass, final UUID uuid) {
    assertClass(vfClass);
    return find(vfClass, uuid).tryNext().isPresent();
  }

  public <T extends AlexandriaVF> T createVF(final Class<T> vfClass) {
    assertClass(vfClass);
    return framedGraph.addVertex(vfClass);
  }

  public <T extends AlexandriaVF> Optional<T> readVF(final Class<T> vfClass, final UUID uuid) {
    assertClass(vfClass);
    return firstOrEmpty(find(vfClass, uuid).toList());
  }

  public <T extends AlexandriaVF> FramedGraphTraversal<Object, T> find(Class<T> vfClass) {
    return framedGraph.V(vfClass);
  }

  // graph methods

  public void removeExpiredTentatives(final Long threshold) {
    graph.traversal().V()//
        .has("state", AlexandriaState.TENTATIVE.name())//
        .has("stateSince", lt(threshold))//
        .forEachRemaining(Element::remove);
  }

  public void removeVertexWithId(final String annotationBodyId) {
    graph.traversal().V()//
        .has(IDENTIFIER_PROPERTY, annotationBodyId).next().remove();
  }

  public void dumpToGraphSON(final OutputStream os) throws IOException {
    graph.io(new GraphSONIo.Builder()).writer().create().writeGraph(os, graph);
  }

  public void dumpToGraphML(final OutputStream os) throws IOException {
    graph.io(new GraphMLIo.Builder()).writer().create().writeGraph(os, graph);
  }

  public void loadFromDisk(final String file) {
    try {
      graph.io(new GraphMLIo.Builder()).readGraph(file);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void saveToDisk(final String file) {
    if (file != null) {
      try {
        graph.io(new GraphMLIo.Builder()).writeGraph(file);
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void setDumpFile(final String dumpfile) {
    this.dumpfile = dumpfile;
  }

  // - private methods - //

  private <T extends AlexandriaVF> FramedGraphTraversal<Object, T> find(final Class<T> vfClass, final UUID uuid) {
    return find(vfClass).has(IDENTIFIER_PROPERTY, uuid.toString());
  }

  private String getDumpFile() {
    return dumpfile;
  }

  private <T> Optional<T> firstOrEmpty(final List<T> results) {
    return results.isEmpty() ? Optional.empty() : Optional.ofNullable(results.get(0));
  }

  private void assertClass(final Class<? extends AlexandriaVF> clazz) {
    if (clazz.getAnnotationsByType(Vertex.class).length == 0) {
      throw new RuntimeException("Class " + clazz + " has no peapod @Vertex annotation, are you sure it is the correct class?");
    }
  }

}
