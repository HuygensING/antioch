package nl.knaw.huygens.alexandria.endpoint.about;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Map;
import java.util.MissingResourceException;
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
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Singleton
@Path("about")
@Api("about")
public class AboutEndpoint extends JSONEndpoint {
  private static final Instant startedAt = Instant.now();

  private static PropertyResourceBundle propertyResourceBundle;

  private final TemporalAmount tentativesTTL;
  private final URI baseURI;

  @Inject
  public AboutEndpoint(AlexandriaConfiguration config, AlexandriaService service) {
    this.baseURI = config.getBaseURI();
    this.tentativesTTL = service.getTentativesTimeToLive();
  }

  private static synchronized String getProperty(String key) {
    if (propertyResourceBundle == null) {
      try {
        propertyResourceBundle = new PropertyResourceBundle(//
            Thread.currentThread().getContextClassLoader().getResourceAsStream("about.properties"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    try {
      return propertyResourceBundle.getString(key);
    } catch (MissingResourceException e) {
      Log.warn("Missing expected resource: [{}] -- winging it", key);
      return "missing";
    } catch (ClassCastException e) {
      Log.warn("Property value for key [{}] cannot be transformed to String -- winging it", key);
      return "malformed";
    }
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
    data.put("buildDate", getProperty("buildDate"));
    data.put("commitId", getProperty("commitId"));
    data.put("scmBranch", getProperty("scmBranch"));
    data.put("startedAt", startedAt.toString());
    data.put("tentativesTTL", tentativesTTL.toString());
    data.put("version", getProperty("version"));
    return Response.ok(data).build();
  }

}
