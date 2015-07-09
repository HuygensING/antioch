package nl.knaw.huygens.alexandria.jersey;

import static java.util.logging.Logger.getAnonymousLogger;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.JsonConfiguration;
import nl.knaw.huygens.alexandria.endpoint.about.AboutEndpoint;

public class AlexandriaApplication extends ResourceConfig {
  public AlexandriaApplication() {
    Log.info("initializing AlexandriaApplication...");
    packages("nl.knaw.huygens.alexandria.endpoint", "nl.knaw.huygens.alexandria.jersey");

    // Bean Validation error messages in the response entity.
    property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

    // Server-side request logging, including entities
    register(new LoggingFilter(getAnonymousLogger(), true));

    // Validation configuration
    // register(ValidationConfigurationContextResolver.class);

    // JSON configuration
    register(JsonConfiguration.class);

    // // X-Jersey-Tracing-nnn diagnostic response headers
    // property(ServerProperties.TRACING, "ALL");

    // initialize to set the startdate
    AboutEndpoint.init();
  }

}
