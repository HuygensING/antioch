package nl.knaw.huygens.alexandria.util;

import javax.inject.Inject;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class CleanupCommand implements Runnable {
  @Inject
  private AlexandriaService service;

  @Override
  public void run() {
    Log.info("removing expired tentatives");
    service.removeExpiredTentatives();
  }
}
