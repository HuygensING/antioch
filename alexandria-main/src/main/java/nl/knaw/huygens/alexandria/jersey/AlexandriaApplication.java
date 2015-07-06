package nl.knaw.huygens.alexandria.jersey;

import static java.util.logging.Logger.getAnonymousLogger;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import nl.knaw.huygens.alexandria.config.JsonConfiguration;

public class AlexandriaApplication extends ResourceConfig {
  public AlexandriaApplication() {
    packages("nl.knaw.huygens.alexandria.endpoint", "nl.knaw.huygens.alexandria.jersey");

    // Bean Validation error messages in the response entity.
    property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

    // // X-Jersey-Tracing-nnn diagnostic response headers
    // property(ServerProperties.TRACING, "ALL");

    // Server-side request logging, including entities
    register(new LoggingFilter(getAnonymousLogger(), true));

    // Validation configuration
    // register(ValidationConfigurationContextResolver.class);

    // JSON configuration
    register(JsonConfiguration.class);
  }

}
