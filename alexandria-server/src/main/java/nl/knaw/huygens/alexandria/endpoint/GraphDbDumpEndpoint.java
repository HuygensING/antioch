package nl.knaw.huygens.alexandria.endpoint;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import nl.knaw.huygens.alexandria.storage.Storage;

@Singleton
@Path("dump")
public class GraphDbDumpEndpoint extends JSONEndpoint {
  @Inject
  Storage storage;

  @GET
  public Response dumpGraphDb() {
    StreamingOutput stream = os -> {
      storage.dump(os);
    };
    return Response.ok(stream).build();
  }
}
