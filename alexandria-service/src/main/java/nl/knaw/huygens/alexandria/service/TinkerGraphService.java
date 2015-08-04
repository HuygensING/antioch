package nl.knaw.huygens.alexandria.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import nl.knaw.huygens.alexandria.storage.Storage;

@Singleton
public class TinkerGraphService extends TinkerPopService {

  protected static final Storage STORAGE = new Storage(TinkerGraph.open());

  @Inject
  public TinkerGraphService() {
    super(STORAGE);
  }
}
