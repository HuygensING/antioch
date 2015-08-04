package nl.knaw.huygens.alexandria.endpoint;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import nl.knaw.huygens.alexandria.service.TinkerPopService;

@Singleton
@Path("dump")
public class GraphDbDumpEndpoint extends JSONEndpoint {
  @Inject
  TinkerPopService service;

  @GET
  @Path("graphson")
  public Response dumpGraphDbAsJSON() {
    StreamingOutput stream = os -> {
      service.dumpToGraphSON(os);
    };
    return Response.ok(stream).build();
  }

  @GET
  @Path("graphml")
  @Produces(MediaType.APPLICATION_XML)
  public Response dumpGraphDbAsGraphML() {
    StreamingOutput stream = os -> {
      service.dumpToGraphML(os);
    };
    return Response.ok(stream).build();
  }
}
