package nl.knaw.huygens.alexandria.endpoint.about;

import static nl.knaw.huygens.alexandria.jaxrs.AlexandriaRoles.CERTIFIED;
import static nl.knaw.huygens.alexandria.jaxrs.AlexandriaRoles.JANITOR;
import static nl.knaw.huygens.alexandria.jersey.AlexandriaApplication.START_TIME_PROPERTY;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.config.PropertiesConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
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
    final Map<String, String> data = new LinkedHashMap<>();
    data.put("baseURI", baseURI.toString());
    data.put("buildDate", properties.getProperty("buildDate").get());
    data.put("commitId", properties.getProperty("commitId").get());
    data.put("scmBranch", properties.getProperty("scmBranch").get());
    data.put("startedAt", STARTED_AT);
    data.put("tentativesTTL", tentativesTTL.toString());
    data.put("version", properties.getProperty("version").get());
    return Response.ok(data).build();
  }

  @GET
  @Path("service")
  @RolesAllowed({CERTIFIED, JANITOR})
  public Response getGraphMetadata() {
    return Response.ok(service.getMetadata()).build();
  }

}
