package nl.knaw.huygens.alexandria.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import jline.internal.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.storage.frames.ResourceVF;
import peapod.FramedGraph;

import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Transaction;

public class Storage {
  private static Graph g;
  private static FramedGraph fg;
  private static boolean transactionsSupported;

  private Transaction tx;

  public Storage(Graph graph) {
    g = graph;
    transactionsSupported = g.features().graph().supportsTransactions();
    fg = new FramedGraph(g, ResourceVF.class.getPackage());
  }

  public boolean exists(Class clazz, UUID uuid) {
    return false;
  }

  public AlexandriaResource read(Class clazz, UUID uuid) {
    if (clazz.equals(AlexandriaResource.class)) {
      List<ResourceVF> results = fg.V(ResourceVF.class).has("id", uuid.toString()).toList();
      if (results.isEmpty()) {
        return null;
      }
      ResourceVF arvf = results.get(0);
      TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(arvf.getProvenanceWho(), arvf.getProvenanceWhen(), arvf.getProvenanceWhy());
      AlexandriaResource resource = new AlexandriaResource(uuid, provenance);
      resource.setRef(arvf.getRef());
      resource.setState(arvf.getState());
      return resource;
    }
    return null;
  }

  public void createOrUpdate(AlexandriaResource resource) {
    startTransaction();

    ResourceVF arvf = null;
    if (exists(resource.getClass(), resource.getId())) {
      arvf = fg.v(resource.getId());
    } else {
      arvf = fg.addVertex(ResourceVF.class, resource.getId());
    }

    arvf.setId(resource.getId().toString());
    arvf.setRef(resource.getRef());
    arvf.setState(resource.getState());

    AlexandriaProvenance provenance = resource.getProvenance();
    arvf.setProvenanceWhen(provenance.getWhen());
    arvf.setProvenanceWho(provenance.getWho());
    arvf.setProvenanceWhy(provenance.getWhy());

    commitTransaction();
  }

  public void dump(OutputStream os) throws IOException {
    g.io().graphSONWriter().create().writeGraph(os, g);
  }

  // private methods
  private void startTransaction() {
    if (transactionsSupported) {
      tx = fg.tx();
    }
  }

  private void commitTransaction() {
    if (transactionsSupported) {
      tx.commit();
      tx.close();
    }
  }

  private void rollbackTransaction() {
    if (transactionsSupported) {
      tx.rollback();
      tx.close();
    } else {
      Log.error("rollback called, but transactions are not supported by graph {}", g);
    }
  }
}
