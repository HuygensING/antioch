package nl.knaw.huygens.alexandria.endpoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.google.common.collect.Maps;

@Singleton
@Path("about")
public class AboutEndpoint extends JSONEndpoint {
  Date starttime = new Date();

  /**
   * Show information about the back-end
   *
   * @return about-data map
   */
  @GET
  public Response getMetadata() {
    Map<String, String> data = Maps.newLinkedHashMap();
    data.put("started", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.starttime));
    return Response.ok(data).build();
  }

}
