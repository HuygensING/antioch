package nl.knaw.huygens.alexandria.endpoint.resource;

import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationCreationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationPrototype;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceAnnotations extends JSONEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceAnnotations.class);

  private final AlexandriaService alexandriaService;

  private final UUID uuid;

	private final AnnotationCreationRequestBuilder requestBuilder;

  @Inject
  public ResourceAnnotations(AlexandriaService alexandriaService, //
  		AnnotationCreationRequestBuilder requestBuilder, //
  		@PathParam("uuid") final UUIDParam uuidParam) {
		LOG.trace("resourceService=[{}], uuidParam=[{}]", alexandriaService, uuidParam);
    this.alexandriaService = alexandriaService;
    this.requestBuilder = requestBuilder;
    this.uuid = uuidParam.getValue();
  }

  @GET
  public Response get() {
    final Set<AlexandriaAnnotation> annotations = alexandriaService.readResource(uuid).getAnnotations();

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
  
  @POST
  public Response addAnnotation(AnnotationPrototype prototype) {
  	return Response.ok().build();
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
