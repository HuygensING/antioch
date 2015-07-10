package nl.knaw.huygens.alexandria.endpoint.about;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;

@Singleton
@Path("about")
@Api("about")
public class AboutEndpoint extends JSONEndpoint {
  private static final Instant startedAt = Instant.now();

  private static PropertyResourceBundle propertyResourceBundle;

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
    data.put("version", getProperty("version"));
    data.put("commitId", getProperty("commit_id"));
    data.put("buildDate", getProperty("buildDate"));
    data.put("startedAt", startedAt.toString());
    return Response.ok(data).build();
  }

}
