package nl.knaw.huygens.alexandria.storage;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.knaw.huygens.Log;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

@Singleton
public class TinkerGraphStorage extends Storage {
  @Inject
  public TinkerGraphStorage() {
    super(TinkerGraph.open());
    if (!persistenceSupported && Files.exists(Paths.get(DUMPFILE))) {
      Log.info("reading stored db from {}", DUMPFILE);
      loadFromDisk(DUMPFILE);
    }
  }
}
