package nl.knaw.huygens.alexandria.endpoint.webannotation;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
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
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import io.swagger.annotations.Api;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationDeprecationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.jaxrs.ThreadContext;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Path(EndpointPaths.WEB_ANNOTATIONS)
@Api("webannotations")
public class WebAnnotationsEndpoint extends JSONEndpoint {
  private static final String WEBANNOTATION_TYPE = "alexandria:webannotation";
  private static final String OA_HAS_TARGET_IRI = "http://www.w3.org/ns/oa#hasTarget";
  private static final String OA_HAS_SOURCE_IRI = "http://www.w3.org/ns/oa#hasSource";
  // private static final String JSONLD_MEDIATYPE = "application/ld+json; profile=\"http://www.w3.org/ns/anno.jsonld\"";
  private static final String JSONLD_MEDIATYPE = "application/ld+json";
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
  private LocationBuilder locationBuilder;
  private AlexandriaConfiguration config;

  @Inject
  public WebAnnotationsEndpoint(AlexandriaService service, //
      LocationBuilder locationBuilder, //
      AlexandriaConfiguration config, //
      AnnotationEntityBuilder entityBuilder, //
      AnnotationDeprecationRequestBuilder requestBuilder) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    this.config = config;
    this.entityBuilder = entityBuilder;
    this.requestBuilder = requestBuilder;
  }

  @GET
  @Produces(JSONLD_MEDIATYPE)
  public Response getWebAnnotations() {
    // WebAnnotation webAnnotation = readExistingWebAnnotation(uuidParam);
    // WebAnnotation webAnnotation = asWebAnnotation(null);
    // return Response.ok(webAnnotations)//
    // .link(RESOURCE_TYPE_URI, "type")//
    // .link(ANNOTATION_TYPE_URI, "type")//
    // .tag(webAnnotation.eTag())//
    // .allow(ALLOWED_METHODS)//
    // .build();
    return Response.ok().build();
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
    WebAnnotation webAnnotation = readExistingWebAnnotation(uuidParam);
    // WebAnnotation webAnnotation = asWebAnnotation(null);
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
      Map<String, Object> jsonObject = (Map<String, Object>) JsonUtils.fromString(json);
      Map context = new HashMap<>();
      JsonLdOptions options = new JsonLdOptions();
      Map<String, Object> compacted = JsonLdProcessor.compact(jsonObject, context, options);
      Map<String, Object> target = (Map<String, Object>) compacted.get(OA_HAS_TARGET_IRI);
      Map<String, Object> source = (Map<String, Object>) target.get(OA_HAS_SOURCE_IRI);
      String resourceRef = (String) source.get("@id");
      UUID resourceUUID = UUID.randomUUID();
      // TODO: use information from prototype to create provenance
      TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(ThreadContext.getUserName(), Instant.now(), AlexandriaProvenance.DEFAULT_WHY);
      // TODO: first see if there's already a resource with this ref, if so, use this.
      service.createOrUpdateResource(resourceUUID, resourceRef, provenance, AlexandriaState.CONFIRMED);
      AlexandriaResource alexandriaResource = service.readResource(resourceUUID).get();

      UUID annotationBodyUUID = UUID.randomUUID();
      AlexandriaAnnotationBody annotationBody = service.createAnnotationBody(annotationBodyUUID, WEBANNOTATION_TYPE, json, provenance);
      AlexandriaAnnotation annotation = service.annotate(alexandriaResource, annotationBody, provenance);

      jsonObject.put("@id", config.getBaseURI() + "/" + EndpointPaths.WEB_ANNOTATIONS + "/" + annotation.getId());
      String json2 = new ObjectMapper().writeValueAsString(jsonObject);
      return new WebAnnotation()//
          .setJson(json2)//
          .setETag(String.valueOf(prototype.getModified().hashCode()));

    } catch (IOException | JsonLdError e) {
      throw new RuntimeException(e);
    }

  }

  private WebAnnotation readExistingWebAnnotation(UUIDParam uuidParam) {
    AlexandriaAnnotation alexandriaAnnotation = service.readAnnotation(uuidParam.getValue()) //
        .orElseThrow(annotationNotFoundForId(uuidParam));
    return asWebAnnotation(alexandriaAnnotation);
  }

  private WebAnnotation asWebAnnotation(AlexandriaAnnotation alexandriaAnnotation) {
    AlexandriaAnnotationBody body = alexandriaAnnotation.getBody();
    String type = body.getType();
    String value = body.getValue();
    String json = "";
    Instant when = alexandriaAnnotation.getProvenance().getWhen();
    if (WEBANNOTATION_TYPE.equals(type)) {
      json = value;

    } else {
      Map<String, Object> webAnnotationMap = Maps.newHashMap();
      webAnnotationMap.put("@context", "http://www.w3.org/ns/anno.jsonld");
      webAnnotationMap.put("id", locationBuilder.locationOf(alexandriaAnnotation));
      webAnnotationMap.put("type", "Annotation");
      webAnnotationMap.put("created", when.toString());
      Map<String, String> bodyMap = ImmutableMap.of("type", "TextualBody", "value", type + ": " + value);
      webAnnotationMap.put("body", bodyMap);
      webAnnotationMap.put("target", locationBuilder.locationOf(alexandriaAnnotation.getAnnotatablePointer()));

      try {
        json = new ObjectMapper().writeValueAsString(webAnnotationMap);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
    return new WebAnnotation()//
        .setJson(json)//
        .setETag(String.valueOf(when.hashCode()));
  }

  static Supplier<NotFoundException> annotationNotFoundForId(Object id) {
    return () -> new NotFoundException(NoAnnotationFoundWithId(id));
  }

  private static String NoAnnotationFoundWithId(Object id) {
    return "No annotation found with id " + id;
  }

}
