package nl.knaw.huygens.alexandria.endpoint.about;

/*
 * #%L
 * alexandria-main
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

import static nl.knaw.huygens.alexandria.jaxrs.AlexandriaRoles.CERTIFIED;
import static nl.knaw.huygens.alexandria.jaxrs.AlexandriaRoles.JANITOR;
import static nl.knaw.huygens.alexandria.jersey.AlexandriaApplication.START_TIME_PROPERTY;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.TemporalAmount;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.config.PropertiesConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.model.AboutEntity;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Singleton
@Path("about")
public class AboutEndpoint extends JSONEndpoint {
  private static final String PROPERTIES_FILE = "about.properties";

  private static final String STARTED_AT = System.getProperty(START_TIME_PROPERTY, Instant.now().toString());

  private final TemporalAmount tentativesTTL;
  private final URI baseURI;
  private final AlexandriaService service;
  private final PropertiesConfiguration properties;

  @Inject
  public AboutEndpoint(AlexandriaConfiguration config, AlexandriaService service) {
    this.service = service;
    this.baseURI = config.getBaseURI();
    this.tentativesTTL = service.getTentativesTimeToLive();
    this.properties = new PropertiesConfiguration(PROPERTIES_FILE, true);
  }

  @GET
  public Response getMetadata() {
    AboutEntity about = new AboutEntity()//
        .setBaseURI(baseURI)//
        .setBuildDate(properties.getProperty("buildDate").get())//
        .setCommitId(properties.getProperty("commitId").get())//
        .setScmBranch(properties.getProperty("scmBranch").get())//
        .setStartedAt(STARTED_AT)//
        .setTentativesTTL(tentativesTTL.toString())//
        .setVersion(properties.getProperty("version").get());
    return Response.ok(about).build();
  }

  @GET
  @Path("service")
  @RolesAllowed({ CERTIFIED, JANITOR })
  public Response getGraphMetadata() {
    return Response.ok(service.getMetadata()).build();
  }

}
