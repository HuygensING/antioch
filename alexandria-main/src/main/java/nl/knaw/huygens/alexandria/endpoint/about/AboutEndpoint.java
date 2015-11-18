package nl.knaw.huygens.alexandria.endpoint.about;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Map;
import java.util.PropertyResourceBundle;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.google.common.collect.Maps;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.config.PropertiesConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.jersey.AlexandriaApplication;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Singleton
@Path("about")
@Api("about")
public class AboutEndpoint extends JSONEndpoint {
  private static final String STARTED_AT = System.getProperty(AlexandriaApplication.STARTTIME_PROPERTY, Instant.now().toString());
  private static PropertyResourceBundle propertyResourceBundle;

  private final TemporalAmount tentativesTTL;
  private final URI baseURI;
  private final AlexandriaService service;
  private PropertiesConfiguration properties;

  @Inject
  public AboutEndpoint(AlexandriaConfiguration config, PropertiesConfiguration pConfig, AlexandriaService service) {
    this.service = service;
    this.baseURI = config.getBaseURI();
    this.tentativesTTL = service.getTentativesTimeToLive();
    this.properties = pConfig;
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
    data.put("buildDate", properties.getProperty("buildDate"));
    data.put("commitId", properties.getProperty("commitId"));
    data.put("scmBranch", properties.getProperty("scmBranch"));
    data.put("startedAt", STARTED_AT);
    data.put("tentativesTTL", tentativesTTL.toString());
    data.put("version", properties.getProperty("version"));
    return Response.ok(data).build();
  }

  /**
   * Show information about the back-end
   *
   * @return about-data map
   */
  @GET
  @Path("service")
  @ApiOperation("get information about the service")
  public Response getGraphMetadata() {
    return Response.ok(service.getMetadata()).build();
  }

}
