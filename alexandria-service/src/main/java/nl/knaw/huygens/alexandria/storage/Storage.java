package nl.knaw.huygens.alexandria.storage;

import java.util.UUID;

import jline.internal.Log;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.storage.frames.AlexandriaResourceVF;
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
    fg = new FramedGraph(g, AlexandriaResourceVF.class.getPackage());
  }

  public boolean exists(Class clazz, UUID uuid) {
    return false;
  }

  public AlexandriaResource read(Class clazz, UUID uuid) {
    if (clazz.equals(AlexandriaResource.class)) {
      startTransaction();
      AlexandriaResourceVF arvf = fg.V(AlexandriaResourceVF.class).has("id", uuid.toString()).toList().get(0);
      commitTransaction();
      TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(arvf.getProvenanceWho(), arvf.getProvenanceWhen(), arvf.getProvenanceWhy());
      AlexandriaResource resource = new AlexandriaResource(uuid, provenance);
      return resource;
    }
    return null;
  }

  public void createOrUpdate(AlexandriaResource resource) {
    startTransaction();
    AlexandriaResourceVF arvf = null;
    if (exists(resource.getClass(), resource.getId())) {
      arvf = fg.v(resource.getId());
    } else {
      arvf = fg.addVertex(AlexandriaResourceVF.class, resource.getId());
    }
    arvf.setId(resource.getId().toString());
    AlexandriaProvenance provenance = resource.getProvenance();
    arvf.setProvenanceWhen(provenance.getWhen());
    arvf.setProvenanceWho(provenance.getWho());
    arvf.setProvenanceWhy(provenance.getWhy());
    arvf.setRef(resource.getRef());
    commitTransaction();
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
