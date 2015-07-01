package nl.knaw.huygens.alexandria.storage;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.knaw.huygens.Log;

@Singleton
public class PersistentTinkerGraphStorage extends TinkerGraphStorage {

  @Inject
  public PersistentTinkerGraphStorage() {
    super();
    if (!supportsPersistence() && Files.exists(Paths.get(DUMPFILE))) {
      Log.info("reading stored db from {}", DUMPFILE);
      loadFromDisk(DUMPFILE);
    }
  }
}
