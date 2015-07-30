package nl.knaw.huygens.alexandria.storage;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;

@Singleton
public class PersistentTinkerGraphService extends TinkerGraphService {
  private static final String DUMPFILE = "alexandria.graphml";

  @Inject
  public PersistentTinkerGraphService(AlexandriaConfiguration config) {
    super();
    String dumpfile = config.getStorageDirectory() + "/" + DUMPFILE;
    setDumpFile(dumpfile);
    if (!supportsPersistence() && Files.exists(Paths.get(dumpfile))) {
      Log.info("reading stored db from {}", dumpfile);
      loadFromDisk(dumpfile);
    }
  }
}
