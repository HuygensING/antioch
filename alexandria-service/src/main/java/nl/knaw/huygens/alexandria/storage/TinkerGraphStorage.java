package nl.knaw.huygens.alexandria.storage;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import nl.knaw.huygens.Log;

@Singleton
public class TinkerGraphStorage extends Storage {

  @Inject
  public TinkerGraphStorage() {
    super(TinkerGraph.open());
    if (!supportsPersistence() && Files.exists(Paths.get(DUMPFILE))) {
      Log.info("reading stored db from {}", DUMPFILE);
      loadFromDisk(DUMPFILE);
    }
  }
}
