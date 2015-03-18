package nl.knaw.huygens.alexandria.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.alexandria.endpoint.param.UUIDParam;
import nl.knaw.huygens.alexandria.service.AnnotationService;

@Path(Annotations.ANNOTATIONS_PATH)
public class Annotations extends JSONEndpoint {
  public static final String ANNOTATIONS_PATH = "annotations";

  private final AnnotationService service;

  public Annotations(@Context AnnotationService service) {
    this.service = service;
  }

  @GET
  @Path("/{uuid}")
  public Response readAnnotation(@PathParam("uuid") UUIDParam uuidParam) {
    return service.getAnnotation(uuidParam.getValue()).map(Response::ok) //
        .orElse(Response.status(Status.NOT_FOUND)).build();
  }

  @PUT
  public Response createAnnotation(String key, String value) {
//    int id = service.createAnnotation(key, value);
//    return Response.created(URI.create(String.valueOf(id))).build();
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }
}



