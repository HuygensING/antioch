package nl.knaw.huygens.alexandria.helpers;

import static java.util.logging.Logger.getAnonymousLogger;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

public class AcceptanceTestApplication extends ResourceConfig {
  public AcceptanceTestApplication() {
    // Bean Validation error messages in the response entity.
    property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

    // X-Jersey-Tracing-nnn diagnostic response headers
    property(ServerProperties.TRACING, "ALL");

    // Server-side request logging, including entities
    register(new LoggingFilter(getAnonymousLogger(), true));

    // JSON configuration
    register(JsonConfiguration.class);
  }
}
