package nl.knaw.huygens.alexandria.endpoint;

import static nl.knaw.huygens.alexandria.endpoint.Annotations.ANNOTATIONS_PATH;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Sets;
import nl.knaw.huygens.alexandria.endpoint.param.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(Resources.RESOURCES_PATH)
public class Resources extends JSONEndpoint {
  public static final String RESOURCES_PATH = "resources";

  private static final URI HERE = URI.create("");

  private static final Logger LOG = LoggerFactory.getLogger(Resources.class);

  private final ResourceService resourceService;
  private final UriInfo uriInfo;

  public Resources(@Context ResourceService resourceService, @Context UriInfo uriInfo) {
    LOG.trace("Resources created, resourceService=[{}], uriInfo=[{}]", resourceService, uriInfo);
    this.uriInfo = uriInfo;
    this.resourceService = resourceService;

    LOG.debug("baseUri: [{}]", uriInfo.getBaseUri().toString());
    LOG.debug("requestUri: [{}]", uriInfo.getRequestUri().toString());
    LOG.debug("absolutePath: [{}]", uriInfo.getAbsolutePath().toString());
  }

  @GET
  @Path("/{uuid}")
  public Response getResourceByID(@PathParam("uuid") final UUIDParam uuid) {
    final AlexandriaResource resource = resourceService.readResource(uuid.getValue());
    return Response.ok(new ResourceAsJSON(resource)).build();
  }

  @POST
  public Response createResourceWithoutGivenID(AlexandriaResource protoType) {
    requireValidEntity(protoType);

    LOG.debug("createResourceWithoutGivenID: protoType=[{}]", protoType);
    LOG.debug("annotations: [{}]", protoType.getAnnotations());

    final AlexandriaResource res = new AlexandriaResource(protoType);//resourceService.createResource(protoType);
    final String id = res.getId().toString();
    return Response.created(URI.create(id)).entity(new ResourceAsJSON(res)).build();
  }

  @PUT
  @Path("/{uuid}")
  public Response createResourceAtSpecificID(@PathParam("uuid") final UUIDParam paramId, AlexandriaResource protoType) {
    requireValidEntity(protoType);
    requireCompatibleIds(paramId, protoType::getId);

    LOG.debug("createResourceAtSpecificID: paramId=[{}] vs protoType.id=[{}]", paramId, protoType.getId());

    resourceService.createResource(protoType);

    return Response.created(HERE).build();
  }

  @Path("/{uuid}/annotations")
  public ResourceAnnotations getAnnotationsForResource(@PathParam("uuid") final UUIDParam uuidParam) {
    return new ResourceAnnotations(resourceService, uuidParam.getValue());
  }

  @GET
  @Path("/{uuid}/ref")
  public Response getResourceRef(@PathParam("uuid") final UUIDParam uuidParam) {
    final String ref = resourceService.readResource(uuidParam.getValue()).getRef();
    return Response.ok(new RefWrapper(ref)).build();
  }

  @JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
  @JsonTypeName("resource")
  class ResourceAsJSON {
    private final UUID id;
    private final String ref;
    private final Set<URI> annotations;
    private final String createdOn;

    public ResourceAsJSON(AlexandriaResource resource) {
      id = resource.getId();
      ref = resource.getRef();
      createdOn = resource.getCreatedOn().toString(); // lest we get the fields of Instant yielded recursively
      annotations = Sets.newHashSet();
      resource.getAnnotations().stream().map(a -> annotationURI(a)).forEach(annotations::add);
    }

    private URI annotationURI(AlexandriaAnnotation a) {
      final String annotationId = a.getId().toString();
      return uriInfo.getBaseUriBuilder().path(ANNOTATIONS_PATH).path(annotationId).build();
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
