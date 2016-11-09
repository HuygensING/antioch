package nl.knaw.huygens.alexandria.endpoint.webannotation;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.swagger.annotations.Api;
import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.QueryField;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.endpoint.JSONEndpoint;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationDeprecationRequestBuilder;
import nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationEntityBuilder;
import nl.knaw.huygens.alexandria.endpoint.search.SearchResult;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
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
  private static final String DEFAULT_PROFILE = "http://www.w3.org/ns/anno.jsonld";
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

  @POST
  @Consumes(JSONLD_MEDIATYPE)
  @Produces(JSONLD_MEDIATYPE)
  public Response addWebAnnotation(WebAnnotationPrototype prototype) {
    prototype.setCreated(Instant.now().toString());
    WebAnnotation webAnnotation = validateAndStore(prototype);
    return Response.created(webAnnotationURI(webAnnotation.getId()))//
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
  public Response getWebAnnotation(@PathParam("uuid") UUIDParam uuidParam, @HeaderParam("Accept") String acceptHeader) {
    Log.info("accept={}", acceptHeader);
    String profile = extractProfile(acceptHeader);
    Log.info("profile={}", profile);
    WebAnnotation webAnnotation = readExistingWebAnnotation(uuidParam);
    return Response.ok(webAnnotation.json())//
        .link(RESOURCE_TYPE_URI, "type")//
        .link(ANNOTATION_TYPE_URI, "type")//
        .tag(webAnnotation.eTag())//
        .allow(ALLOWED_METHODS)//
        .build();
  }

  @DELETE
  @Path("{uuid}")
  public Response deleteWebAnnotation(@PathParam("uuid") UUIDParam uuidParam) {
    UUID annotationUUID = uuidParam.getValue();
    AlexandriaAnnotation annotation = service.readAnnotation(annotationUUID)//
        .orElseThrow(() -> new NotFoundException());
    service.deleteAnnotation(annotation);
    return Response.noContent().build();
  }

  @GET
  @Produces(JSONLD_MEDIATYPE)
  public Response getWebAnnotations() {
    // TODO
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

  @GET
  @Path("search")
  @Produces(JSONLD_MEDIATYPE)
  public Response getSearchResults(@QueryParam("uri") String uri) {
    Log.info("uri={}", uri);
    AlexandriaQuery query = new AlexandriaQuery()//
        .setPageSize(1000)//
        .setFind("annotation")//
        .setWhere("type:eq(\"" + WEBANNOTATION_TYPE + "\") resource.ref:eq(\"" + uri + "\")")//
        .setReturns("id,value");

    SearchResult result = service.execute(query);
    List<Object> webannotations = Lists.newArrayList();
    result.getResults().forEach(resultMap -> {
      String json = (String) resultMap.get("value");
      UUID uuid = UUID.fromString((String) resultMap.get("id"));
      try {
        Map<String, Object> map = enrichJson(json);
        map.put("@id", webAnnotationURI(uuid));
        webannotations.add(map);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    return Response.ok(webannotations).build();
  }

  // private methods
  private Map<String, Object> enrichJson(String json) throws IOException {
    Map<String, Object> map = new ObjectMapper().readValue(json, Map.class);
    return map;
  }

  private static final Pattern PROFILE_PATTERN = Pattern.compile(".*profile=\"(.*?)\".*");

  private String extractProfile(String accept) {
    Matcher matcher = PROFILE_PATTERN.matcher(accept);
    if (matcher.matches()) {
      return matcher.group(1);
    }
    return DEFAULT_PROFILE;
  }

  private WebAnnotation validateAndStore(WebAnnotationPrototype prototype) {
    try {
      String json = new ObjectMapper().writeValueAsString(prototype);
      Map<String, Object> jsonObject = (Map<String, Object>) JsonUtils.fromString(json);

      String resourceRef = extractResourceRef(jsonObject);
      AlexandriaResource alexandriaResource = extractAlexandriaResource(resourceRef);
      AlexandriaAnnotation annotation = createWebAnnotation(json, alexandriaResource);

      jsonObject.put("@id", webAnnotationURI(annotation.getId()));
      String json2 = new ObjectMapper().writeValueAsString(jsonObject);
      return new WebAnnotation(annotation.getId())//
          .setJson(json2)//
          .setETag(String.valueOf(prototype.getModified().hashCode()));

    } catch (IOException | JsonLdError e) {
      throw new BadRequestException(e.getMessage());
    }

  }

  private AlexandriaAnnotation createWebAnnotation(String json, AlexandriaResource alexandriaResource) {
    UUID annotationBodyUUID = UUID.randomUUID();
    // TODO: use information from prototype to create provenance
    TentativeAlexandriaProvenance annotationProvenance = new TentativeAlexandriaProvenance(ThreadContext.getUserName(), Instant.now(), AlexandriaProvenance.DEFAULT_WHY);
    AlexandriaAnnotationBody annotationBody = service.createAnnotationBody(annotationBodyUUID, WEBANNOTATION_TYPE, json, annotationProvenance);
    AlexandriaAnnotation annotation = service.annotate(alexandriaResource, annotationBody, annotationProvenance);
    service.confirmAnnotation(annotation.getId());
    return annotation;
  }

  private String extractResourceRef(Map<String, Object> jsonObject) throws JsonLdError {
    Map<Object, Object> context = new HashMap<>();
    JsonLdOptions options = new JsonLdOptions();
    Map<String, Object> compacted = JsonLdProcessor.compact(jsonObject, context, options);
    Map<String, Object> target = (Map<String, Object>) compacted.get(OA_HAS_TARGET_IRI);
    Map<String, Object> source = (Map<String, Object>) target.get(OA_HAS_SOURCE_IRI);
    String resourceRef = (String) source.get("@id");
    return resourceRef;
  }

  private AlexandriaResource extractAlexandriaResource(String resourceRef) {
    TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(ThreadContext.getUserName(), Instant.now(), AlexandriaProvenance.DEFAULT_WHY);
    // first see if there's already a resource with this ref, if so, use this.
    UUID resourceUUID;
    AlexandriaQuery query = new AlexandriaQuery()//
        .setPageSize(2)//
        .setFind("annotation")//
        .setWhere("resource.ref:eq(\"" + resourceRef + "\")")//
        .setReturns(QueryField.resource_id.externalName());

    List<Map<String, Object>> results = service.execute(query).getResults();
    if (results.isEmpty()) {
      resourceUUID = UUID.randomUUID();
      service.createOrUpdateResource(resourceUUID, resourceRef, provenance, AlexandriaState.CONFIRMED);

    } else {
      String resourceId = (String) results.get(0).get(QueryField.resource_id.externalName());
      resourceUUID = UUID.fromString(resourceId);
    }

    AlexandriaResource alexandriaResource = service.readResource(resourceUUID).get();
    return alexandriaResource;
  }

  private URI webAnnotationURI(UUID annotationUUID) {
    return UriBuilder.fromUri(config.getBaseURI())//
        .path(EndpointPaths.WEB_ANNOTATIONS)//
        .path(annotationUUID.toString())//
        .build();
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
      json = addIdToValue(alexandriaAnnotation, value);

    } else {
      Map<String, Object> webAnnotationMap = Maps.newHashMap();
      webAnnotationMap.put("@context", "http://www.w3.org/ns/anno.jsonld");
      webAnnotationMap.put("@id", locationBuilder.locationOf(alexandriaAnnotation));
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
    return new WebAnnotation(alexandriaAnnotation.getId())//
        .setJson(json)//
        .setETag(String.valueOf(when.hashCode()));
  }

  private String addIdToValue(AlexandriaAnnotation alexandriaAnnotation, String value) {
    String json = value;
    try {
      Map<String, Object> webAnnotationMap = enrichJson(json);
      webAnnotationMap.put("@id", webAnnotationURI(alexandriaAnnotation.getId()));
      json = new ObjectMapper().writeValueAsString(webAnnotationMap);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return json;
  }

  static Supplier<NotFoundException> annotationNotFoundForId(Object id) {
    return () -> new NotFoundException(NoAnnotationFoundWithId(id));
  }

  private static String NoAnnotationFoundWithId(Object id) {
    return "No annotation found with id " + id;
  }

}
