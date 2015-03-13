package nl.knaw.huygens.alexandria.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;

import nl.knaw.huygens.alexandria.service.AnnotationService;

@Path("/annotations")
public class Annotations {
  private final AnnotationService service = new AnnotationService();

  @POST
  public Response createAnnotation(String key, String value) {
    int id = service.createAnnotation(key, value);
    return Response.created(URI.create(String.valueOf(id))).build();
  }

  @GET
  @Path("/{id}")
  public Response readAnnotation(@PathParam("id") int id) {
    return service.getAnnotation(id).map(AnnotationService.Anno::toString).map(Response::ok) //
        .orElse(Response.status(Status.NOT_FOUND)).build();
  }
}



