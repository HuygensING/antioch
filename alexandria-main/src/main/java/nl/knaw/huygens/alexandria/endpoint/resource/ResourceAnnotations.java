package nl.knaw.huygens.alexandria.endpoint.resource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Set;
import java.util.UUID;

import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceAnnotations extends JSONEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceAnnotations.class);

  private final ResourceService resourceService;

  private final UUID uuid;

  @Inject
  public ResourceAnnotations(ResourceService resourceService, @PathParam("uuid") final UUIDParam uuidParam) {
    LOG.trace("resourceService=[{}], uuidParam=[{}]", resourceService, uuidParam);
    this.resourceService = resourceService;
    this.uuid = uuidParam.getValue();
  }

  @GET
  public Response get() {
    final Set<AlexandriaAnnotation> annotations = resourceService.readResource(uuid).getAnnotations();

    /*
      return Response.ok(annotations).build() unfortunately yields:

      [{ "id" : "42f22020-cc82-11e4-aec9-1be06e873083", "annotations" : []},
       { "id" : "92af4396-cc77-11e4-9b9b-1f1561a91434", "annotations" : []}]

      i.e.,
        1) without the desired outer "annotations" wrapper, and
        2) without the desired "annotation" wrapper around each annotation.

      Solution (for now?) is to use a static wrapper class (q.v.).
     */

    return Response.ok(new AnnotationsView(annotations)).build();
  }

  static class AnnotationsView {
    private final Set<AlexandriaAnnotation> annotations;

    public AnnotationsView(Set<AlexandriaAnnotation> annotations) {
      this.annotations = annotations;
    }

    public Set<AlexandriaAnnotation> getAnnotations() {
      return annotations;
    }
  }
}
