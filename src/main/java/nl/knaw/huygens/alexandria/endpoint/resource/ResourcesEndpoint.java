package nl.knaw.huygens.alexandria.endpoint.resource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Sets;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(EndpointPaths.RESOURCES)
public class ResourcesEndpoint extends JSONEndpoint {
  private static final URI HERE = URI.create("");

  private static final Logger LOG = LoggerFactory.getLogger(ResourcesEndpoint.class);

  private final ResourceService resourceService;
  private final AlexandriaConfiguration config;

  public ResourcesEndpoint(@Context AlexandriaConfiguration config, @Context ResourceService resourceService) {
    LOG.trace("Resources created, resourceService=[{}], config=[{}]", resourceService, config);

    this.config = config;
    this.resourceService = resourceService;
  }

  @GET
  @Path("/{uuid}")
  public Response getResourceByID(@PathParam("uuid") final UUIDParam uuid) {
    final AlexandriaResource resource = resourceService.readResource(uuid.getValue());
    return Response.ok(new ResourceAsJSON(resource)).build();
  }

  @POST
  public Response createResourceWithoutGivenID(AlexandriaResource protoType) {
    requireEntity(protoType);

    LOG.debug("createResourceWithoutGivenID: protoType=[{}]", protoType);
    LOG.debug("annotations: [{}]", protoType.getAnnotations());

    final AlexandriaResource res = new AlexandriaResource(protoType);//resourceService.createResource(protoType);
    final String id = res.getId().toString();
    return Response.created(URI.create(id)).entity(new ResourceAsJSON(res)).build();
  }

  @PUT
  @Path("/{uuid}")
  public Response createResourceAtSpecificID(@PathParam("uuid") final UUIDParam paramId, AlexandriaResource protoType) {
    requireEntity(protoType);
    requireCompatibleIds(paramId, protoType::getId);

    LOG.debug("createResourceAtSpecificID: paramId=[{}] vs protoType.id=[{}]", paramId, protoType.getId());

    resourceService.createResource(protoType);

    return Response.created(HERE).build();
  }

  @Path("/{uuid}/annotations")
  public ResourceAnnotations getAnnotationsForResource(@PathParam("uuid") final UUIDParam uuidParam) {
    return annotationsFor(uuidParam.getValue());
  }

  @GET
  @Path("/{uuid}/ref")
  public Response getResourceRef(@PathParam("uuid") final UUIDParam uuidParam) {
    final String ref = resourceService.readResource(uuidParam.getValue()).getRef();
    return Response.ok(new RefWrapper(ref)).build();
  }

  private ResourceAnnotations annotationsFor(UUID uuid) {
    return new ResourceAnnotations(resourceService, uuid);
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
      annotationsOf(resource).map(this::annotationURI).forEach(annotations::add);
    }

    private Stream<AlexandriaAnnotation> annotationsOf(AlexandriaResource resource) {
      return resource.getAnnotations().stream();
    }

    private URI annotationURI(AlexandriaAnnotation a) {
      final String annotationId = a.getId().toString();
      return UriBuilder.fromUri(config.getBaseURI()).path(EndpointPaths.ANNOTATIONS).path(annotationId).build();
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
