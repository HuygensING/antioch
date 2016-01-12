package nl.knaw.huygens.alexandria;

/*
 * #%L
 * alexandria-service
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static java.util.logging.Logger.getAnonymousLogger;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import nl.knaw.huygens.alexandria.config.JsonConfiguration;
import nl.knaw.huygens.alexandria.config.ValidationConfigurationContextResolver;

public class TestApplication extends ResourceConfig {
  public TestApplication() {
    // Bean Validation error messages in the response entity.
    property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

    // X-Jersey-Tracing-nnn diagnostic response headers
    property(ServerProperties.TRACING, "ALL");

    // Server-side request logging, including entities
    register(new LoggingFilter(getAnonymousLogger(), true));

    // Validation configuration
    register(ValidationConfigurationContextResolver.class);

    // JSON configuration
    register(JsonConfiguration.class);
  }
}
