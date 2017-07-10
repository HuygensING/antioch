package nl.knaw.huygens.alexandria.endpoint.about;

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

import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.AboutEntity;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.config.PropertiesConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Singleton
@Path(EndpointPaths.ABOUT)
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
    return ok(about);
  }

  @GET
  @Path("service")
  @RolesAllowed({ CERTIFIED, JANITOR })
  public Response getGraphMetadata() {
    return ok(service.getMetadata());
  }

}
