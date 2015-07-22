package nl.knaw.huygens.alexandria.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {
  private Scheduler() {
  }

  public static void scheduleExpiredTentativesRemoval() {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    CleanupCommand cleanupCommand = new CleanupCommand();
    scheduledExecutorService.scheduleAtFixedRate(cleanupCommand, 0, 1, TimeUnit.HOURS);
  }

}
