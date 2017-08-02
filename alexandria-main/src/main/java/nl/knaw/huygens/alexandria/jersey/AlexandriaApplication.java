package nl.knaw.huygens.alexandria.jersey;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static java.util.logging.Logger.getAnonymousLogger;

import java.time.Instant;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.huygens.alexandria.config.JsonConfiguration;
import nl.knaw.huygens.alexandria.config.ValidationConfigurationContextResolver;
import nl.knaw.huygens.alexandria.jaxrs.AuthenticationRequestFilter;
import nl.knaw.huygens.alexandria.jaxrs.AuthorizationRequestFilter;
import nl.knaw.huygens.alexandria.jaxrs.CORSFilter;

@ApplicationPath("/")
public class AlexandriaApplication extends ResourceConfig {
  private static final Logger LOG = LoggerFactory.getLogger(AlexandriaApplication.class);
  public static final String START_TIME_PROPERTY = "alexandria.startTime";

  public AlexandriaApplication() {
    LOG.info("initializing AlexandriaApplication...");
    packages("nl.knaw.huygens.alexandria.endpoint", "nl.knaw.huygens.alexandria.jersey");

    // Bean Validation error messages in the response entity.
    property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

    // Server-side request logging, including entities
    register(new LoggingFilter(getAnonymousLogger(), true));

    // Authentication and Authorization
    register(AuthenticationRequestFilter.class);
    register(AuthorizationRequestFilter.class);
    register(RolesAllowedDynamicFeature.class);
    register(CORSFilter.class);

    // Validation configuration
    register(ValidationConfigurationContextResolver.class);

    // JSON configuration
    register(JsonConfiguration.class);

    // // X-Jersey-Tracing-nnn diagnostic response headers
    // property(ServerProperties.TRACING, "ALL");

    // disable output buffering: no automatic Content-Length header.
    // property(ServerProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, 0);

    System.setProperty(AlexandriaApplication.START_TIME_PROPERTY, Instant.now().toString());
  }

}
