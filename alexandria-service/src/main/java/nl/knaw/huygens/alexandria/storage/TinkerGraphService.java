package nl.knaw.huygens.alexandria.storage;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

@Singleton
public class TinkerGraphService extends TinkerPopService {

  @Inject
  public TinkerGraphService() {
    super(TinkerGraph.open());
  }
}
