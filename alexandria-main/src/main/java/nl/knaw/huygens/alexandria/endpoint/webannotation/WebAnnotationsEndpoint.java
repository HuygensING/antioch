package nl.knaw.huygens.alexandria.endpoint.webannotation;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;

import io.swagger.annotations.Api;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationDeprecationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Path(EndpointPaths.WEB_ANNOTATIONS)
@Api("webannotations")
public class WebAnnotationsEndpoint extends JSONEndpoint {
  private static final String JSONLD_MEDIATYPE = "application/ld+json; profile=\"http://www.w3.org/ns/anno.jsonld\"";
  private static final String RESOURCE_TYPE_URI = "http://www.w3.org/ns/ldp#Resource";
  private static final String ANNOTATION_TYPE_URI = "http://www.w3.org/ns/oa#Annotation";
  private static final Set<String> ALLOWED_METHODS = ImmutableSet.<String> of("PUT", "GET", "OPTIONS", "HEAD", "DELETE");
  private static final String DEFAULT_WEBANNOTATION = "{"//
      + "\"@context\": \"http://www.w3.org/ns/anno.jsonld\","//
      + "\"id\": \"http://example.org/annotations/anno1\","//
      + "\"type\": \"Annotation\","//
      + "\"created\": \"2015-01-31T12:03:45Z\","//
      + "\"body\": {"//
      + "\"type\": \"TextualBody\","//
      + "\"value\": \"I like this page!\""//
      + "},"//
      + "\"target\": \"http://www.example.com/index.html\"}";

  private final AlexandriaService service;
  private final AnnotationEntityBuilder entityBuilder;
  private final AnnotationDeprecationRequestBuilder requestBuilder;

  @Inject
  public WebAnnotationsEndpoint(AlexandriaService service, //
      AnnotationEntityBuilder entityBuilder, //
      AnnotationDeprecationRequestBuilder requestBuilder) {
    this.service = service;
    this.entityBuilder = entityBuilder;
    this.requestBuilder = requestBuilder;
  }

  @POST
  @Consumes(JSONLD_MEDIATYPE)
  @Produces(JSONLD_MEDIATYPE)
  public Response addWebAnnotation(WebAnnotationPrototype prototype) {
    UUID uuid = UUID.randomUUID();
    prototype.setId("http://localhost:2015/webannotations/" + uuid)//
        .setCreated(Instant.now().toString());
    WebAnnotation webAnnotation = validate(prototype);
    return Response.created(URI.create(prototype.getId()))//
        .link(RESOURCE_TYPE_URI, "type")//
        .link(ANNOTATION_TYPE_URI, "type")//
        .tag(webAnnotation.eTag())//
        .allow(ALLOWED_METHODS)//
        .entity(webAnnotation.json())//
        .build();
  }

  @GET
  @Path("{uuid}")
  @Produces(JSONLD_MEDIATYPE)
  public Response getWebAnnotation(@PathParam("uuid") UUIDParam uuidParam) {
    // WebAnnotation webAnnotation = readExistingWebAnnotation(uuidParam);
    WebAnnotation webAnnotation = asWebAnnotation(null);
    return Response.ok(webAnnotation.json())//
        .link(RESOURCE_TYPE_URI, "type")//
        .link(ANNOTATION_TYPE_URI, "type")//
        .tag(webAnnotation.eTag())//
        .allow(ALLOWED_METHODS)//
        .build();
  }

  @GET
  @Path("search")
  @Produces(JSONLD_MEDIATYPE)
  public Response getSearchResults(@QueryParam("uri") String uri) {
    Log.info("uri={}", uri);
    return Response.ok().build();
  }

  // private methods

  private WebAnnotation validate(WebAnnotationPrototype prototype) {
    String json = "";
    try {
      json = new ObjectMapper().writeValueAsString(prototype);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return new WebAnnotation()//
        .setJson(json)//
        .setETag(String.valueOf(prototype.getModified().hashCode()));
  }

  private WebAnnotation readExistingWebAnnotation(UUIDParam uuidParam) {
    AlexandriaAnnotation alexandriaAnnotation = service.readAnnotation(uuidParam.getValue()) //
        .orElseThrow(annotationNotFoundForId(uuidParam));
    return asWebAnnotation(alexandriaAnnotation);
  }

  private WebAnnotation asWebAnnotation(AlexandriaAnnotation alexandriaAnnotation) {
    String json = DEFAULT_WEBANNOTATION;
    return new WebAnnotation()//
        .setJson(json)//
        .setETag(String.valueOf(alexandriaAnnotation.getProvenance().getWhen().hashCode()));
  }

  static Supplier<NotFoundException> annotationNotFoundForId(Object id) {
    return () -> new NotFoundException(NoAnnotationFoundWithId(id));
  }

  private static String NoAnnotationFoundWithId(Object id) {
    return "No annotation found with id " + id;
  }

}
