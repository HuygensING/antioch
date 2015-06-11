package nl.knaw.huygens.alexandria.storage;

import java.util.UUID;

import nl.knaw.huygens.alexandria.frames.AlexandriaResourceVF;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import peapod.FramedGraph;

import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Transaction;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

public class Storage {
  Graph g = TinkerGraph.open();
  FramedGraph fg = new FramedGraph(g, Package.getPackage("nl.knaw.huygens.alexandria.frames"));

  public boolean exists(Class clazz, UUID uuid) {
    return false;
  }

  public AlexandriaResource read(Class clazz, UUID uuid) {
    if (clazz.equals(AlexandriaResource.class)) {
      Transaction tx = fg.tx();
      AlexandriaResourceVF arvf = fg.v(uuid.toString());
      tx.close();
      TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(arvf.getProvenanceWho(), arvf.getProvenanceWhen(), arvf.getProvenanceWhy());
      AlexandriaResource resource = new AlexandriaResource(uuid, provenance);
      return resource;
    }
    return null;
  }

  public void createOrUpdate(AlexandriaResource resource) {
    AlexandriaResourceVF arvf = null;
    Transaction tx = fg.tx();
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
    tx.commit();
  }
}
