package nl.knaw.huygens.alexandria.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Set;

import nl.knaw.huygens.alexandria.endpoint.param.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.ResourceService;

@Path("/resources")
public class Resources extends JSONEndpoint {
  public static final URI HERE = URI.create("");

  private final ResourceService resourceService;

  public Resources(@Context ResourceService resourceService) {
    this.resourceService = resourceService;
  }

  @GET
  @Path("/{uuid}")
  public Response getResourceByID(@PathParam("uuid") final UUIDParam uuid) {
    return Response.ok(resourceService.readResource(uuid.getValue())).build();
  }

  @GET
  @Path("/{uuid}/annotations")
  public Response getAnnotationsForResource(@PathParam("uuid") final UUIDParam uuid) {
    final Set<AlexandriaAnnotation> annotations = resourceService.readResource(uuid.getValue()).getAnnotations();

    /*
      return Response.ok(annotations).build() unfortunately yields:
      [{ "id" : "42f22020-cc82-11e4-aec9-1be06e873083", "annotations" : []},
       { "id" : "92af4396-cc77-11e4-9b9b-1f1561a91434", "annotations" : []}]

      i.e.,
        1) without the desired outer "annotations" wrapper, and
        2) without the desired "annotation" wrapper around each annotation.

      Solution (for now?) is to use a static wrapper class (q.v.).
     */

    return Response.ok(new AnnotationsWrapper(annotations)).build();
  }

  @GET
  @Path("/{uuid}/ref")
  public Response getResourceRef(@PathParam("uuid") final UUIDParam uuid) {
    final String ref = resourceService.readResource(uuid.getValue()).getRef();
    return Response.ok(new RefWrapper(ref)).build();
  }

  @POST
  public Response createResourceWithoutGivenID(AlexandriaResource protoType) {
    requireValidEntity(protoType);

    System.err.println("createResourceWithoutGivenID: protoType=" + protoType);
    System.err.println("annotations: " + protoType.getAnnotations());

    final AlexandriaResource res = new AlexandriaResource(protoType);//resourceService.createResource(protoType);
    final String id = res.getId().toString();
    return Response.created(URI.create(id)).entity(res).build();
  }

  @PUT
  @Path("/{uuid}")
  public Response createResourceAtSpecificID(@PathParam("uuid") final UUIDParam paramId, AlexandriaResource protoType) {
    requireValidEntity(protoType);
    requireCompatibleIds(paramId, protoType::getId);

    System.err.println("createResourceAtSpecificID: paramId=" + paramId + " vs protoType.id=" + protoType.getId());

    resourceService.createResource(protoType);

    return Response.created(HERE).build();
  }

  static class AnnotationsWrapper {
    private final Set<AlexandriaAnnotation> annotations;

    public AnnotationsWrapper(Set<AlexandriaAnnotation> annotations) {
      this.annotations = annotations;
    }

    public Set<AlexandriaAnnotation> getAnnotations() {
      return annotations;
    }
  }

  static class RefWrapper {
    private final String ref;

    public RefWrapper(String ref) {
      this.ref = ref;
    }

    public String getRef() {
      return ref;
    }
  }
}
