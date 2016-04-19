package nl.knaw.huygens.alexandria.health;

import com.codahale.metrics.health.HealthCheck;

public class AboutHealthCheck extends HealthCheck {
  @Override
  protected Result check() throws Exception {
    return Result.healthy();
  }
}
