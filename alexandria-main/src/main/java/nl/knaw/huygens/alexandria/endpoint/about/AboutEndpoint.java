package nl.knaw.huygens.alexandria.endpoint.about;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 Huygens ING (KNAW)
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

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.PropertyResourceBundle;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.google.common.collect.Maps;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.jaxrs.Annotations.AuthorizationRequired;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Singleton
@Path("about")
@Api("about")
public class AboutEndpoint extends JSONEndpoint {
  private static final String PROPERTIES_FILE = "about.properties";
  private static final String STARTED_AT = System.getProperty(AlexandriaApplication.STARTTIME_PROPERTY, Instant.now().toString());
  private final TemporalAmount tentativesTTL;
  private final URI baseURI;
  private final AlexandriaService service;
  private final PropertiesConfig properties;

  @Inject
  public AboutEndpoint(AlexandriaConfiguration config, AlexandriaService service) {
    this.service = service;
    this.baseURI = config.getBaseURI();
    this.tentativesTTL = service.getTentativesTimeToLive();
    this.properties = new PropertiesConfig(PROPERTIES_FILE);
  }

  /**
   * Show information about the back-end
   *
   * @return about-data map
   */
  @GET
  @ApiOperation("get information about the server (version,buildDate,commitId,startedAt)")
  public Response getMetadata() {
    final Map<String, String> data = Maps.newLinkedHashMap();
    data.put("baseURI", baseURI.toString());
    data.put("buildDate", properties.getProperty("buildDate").get());
    data.put("commitId", properties.getProperty("commitId").get());
    data.put("scmBranch", properties.getProperty("scmBranch").get());
    data.put("startedAt", STARTED_AT);
    data.put("tentativesTTL", tentativesTTL.toString());
    data.put("version", properties.getProperty("version").get());
    return Response.ok(data).build();
  }

  /**
   * Show information about the back-end
   *
   * @return about-data map
   */
  @AuthorizationRequired
  @GET
  @Path("service")
  @ApiOperation("get information about the service")
  public Response getGraphMetadata() {
    return Response.ok(service.getMetadata()).build();
  }

  public class PropertiesConfig {
    private PropertyResourceBundle propertyResourceBundle;

    public PropertiesConfig(String propertiesFile) {
      try {
        propertyResourceBundle = new PropertyResourceBundle(//
            Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile));
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("Couldn't read " + propertiesFile);
      }
    }

    public synchronized Optional<String> getProperty(String key) {
      try {
        return Optional.ofNullable(propertyResourceBundle.getString(key));
      } catch (MissingResourceException e) {
        Log.warn("Missing expected resource: [{}] -- winging it", key);
        return Optional.empty();
      } catch (ClassCastException e) {
        Log.warn("Property value for key [{}] cannot be transformed to String", key);
        return Optional.empty();
      }
    }

  }

}
