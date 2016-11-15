package nl.knaw.huygens.alexandria.endpoint.webannotation;

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
import nl.knaw.huygens.alexandria.endpoint.search.SearchResult;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.exception.NotFoundException;
import nl.knaw.huygens.alexandria.jaxrs.ThreadContext;
import nl.knaw.huygens.alexandria.model.*;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path(EndpointPaths.WEB_ANNOTATIONS)
@Api("webannotations")
public class WebAnnotationsEndpoint extends JSONEndpoint {
  private static final String WEBANNOTATION_TYPE = "alexandria:webannotation";
  private static final String OA_HAS_TARGET_IRI = "http://www.w3.org/ns/oa#hasTarget";
  private static final String OA_HAS_SOURCE_IRI = "http://www.w3.org/ns/oa#hasSource";
  private static final String DEFAULT_PROFILE = "http://www.w3.org/ns/anno.jsonld";
  private static final String JSONLD_MEDIATYPE = "application/ld+json";
  private static final String RESOURCE_TYPE_URI = "http://www.w3.org/ns/ldp#Resource";
  private static final String ANNOTATION_TYPE_URI = "http://www.w3.org/ns/oa#Annotation";
  private static final Set<String> ALLOWED_METHODS = ImmutableSet.<String>of("PUT", "GET", "OPTIONS", "HEAD", "DELETE");

  private final AlexandriaService service;
  private LocationBuilder locationBuilder;
  private AlexandriaConfiguration config;

  @Inject
  public WebAnnotationsEndpoint(AlexandriaService service, //
                                LocationBuilder locationBuilder, //
                                AlexandriaConfiguration config) {
    this.service = service;
    this.locationBuilder = locationBuilder;
    this.config = config;
  }

  @POST
  @Consumes(JSONLD_MEDIATYPE)
  @Produces(JSONLD_MEDIATYPE)
  public Response addWebAnnotation(//
                                   @HeaderParam("Accept") String acceptHeader, //
                                   WebAnnotationPrototype prototype//
  ) {
    String expectedProfile = extractProfile(acceptHeader);
    prototype.setCreated(Instant.now().toString());
    WebAnnotation webAnnotation = validateAndStore(prototype);
    String profiledWebAnnotation = profile(webAnnotation.json(), expectedProfile);
    return Response.created(webAnnotationURI(webAnnotation.getId()))//
      .link(RESOURCE_TYPE_URI, "type")//
      .link(ANNOTATION_TYPE_URI, "type")//
      .tag(webAnnotation.eTag())//
      .allow(ALLOWED_METHODS)//
      .entity(profiledWebAnnotation)//
      .build();
  }

  @GET
  @Path("{uuid}")
  @Produces(JSONLD_MEDIATYPE)
  public Response getWebAnnotation(//
                                   @HeaderParam("Accept") String acceptHeader, //
                                   @PathParam("uuid") UUIDParam uuidParam //
  ) {
    String expectedProfile = extractProfile(acceptHeader);
    AlexandriaAnnotation alexandriaAnnotation = service.readAnnotation(uuidParam.getValue()) //
      .orElseThrow(annotationNotFoundForId(uuidParam));
    if (alexandriaAnnotation.getState().equals(AlexandriaState.DELETED)) {
      return Response.status(Status.GONE).build();
    }

    WebAnnotation webAnnotation = asWebAnnotation(alexandriaAnnotation);
    String profiledWebAnnotation = profile(webAnnotation.json(), expectedProfile);
    return Response.ok(profiledWebAnnotation)//
      .link(RESOURCE_TYPE_URI, "type")//
      .link(ANNOTATION_TYPE_URI, "type")//
      .tag(webAnnotation.eTag())//
      .allow(ALLOWED_METHODS)//
      .build();
  }

  @PUT
  @Path("{uuid}")
  @Consumes(JSONLD_MEDIATYPE)
  @Produces(JSONLD_MEDIATYPE)
  public Response updateWebAnnotation(//
                                      @HeaderParam("Accept") String acceptHeader, //
                                      @PathParam("uuid") UUIDParam uuidParam, //
                                      WebAnnotationPrototype prototype//
  ) {
    // TODO: what if the resource changes?
    String expectedProfile = extractProfile(acceptHeader);
    Instant modificationInstant = Instant.now();
    prototype.setModified(modificationInstant.toString());
    UUID annotationUuid = uuidParam.getValue();
    AlexandriaAnnotation alexandriaAnnotation = service.readAnnotation(annotationUuid) //
      .orElseThrow(annotationNotFoundForId(uuidParam));
    try {
      String json = new ObjectMapper().writeValueAsString(prototype);
      TentativeAlexandriaProvenance provenance = new TentativeAlexandriaProvenance(ThreadContext.getUserName(), modificationInstant, AlexandriaProvenance.DEFAULT_WHY);
      AlexandriaAnnotationBody body = service.findAnnotationBodyWithTypeAndValue(WEBANNOTATION_TYPE, json)//
        .orElseGet(() -> new AlexandriaAnnotationBody(UUID.randomUUID(), WEBANNOTATION_TYPE, json, provenance));
      AlexandriaAnnotation newAnnotation = new AlexandriaAnnotation(annotationUuid, body, provenance);
      alexandriaAnnotation = service.deprecateAnnotation(annotationUuid, newAnnotation);
      WebAnnotation webAnnotation = asWebAnnotation(alexandriaAnnotation);
      String profiledWebAnnotation = profile(webAnnotation.json(), expectedProfile);
      return Response.ok(profiledWebAnnotation)//
        .link(RESOURCE_TYPE_URI, "type")//
        .link(ANNOTATION_TYPE_URI, "type")//
        .tag(webAnnotation.eTag())//
        .allow(ALLOWED_METHODS)//
        .build();

    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

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

  // @GET
  // @Produces(JSONLD_MEDIATYPE)
  // public Response getAllWebAnnotations() {
  // // TODO
  // return Response.ok().build();
  // }

  @GET
  @Path("search")
  @Produces(JSONLD_MEDIATYPE)
  public Response getSearchResults(@QueryParam("uri") String uri) {
    List<Object> webannotations = findWebAnnotationsAbout(uri);
    return Response.ok(webannotations).build();
  }

  // private methods

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

  private String profile(String json, String expectedProfile) {
    try {
      Map<String, Object> jsonObject = (Map<String, Object>) JsonUtils.fromString(json);
      JsonLdOptions options = new JsonLdOptions();
      List<Object> expanded = JsonLdProcessor.expand(jsonObject, options);
      Map<String, Object> profiled = JsonLdProcessor.compact(expanded, expectedProfile, options);
      profiled.put("@context", expectedProfile);
      return new ObjectMapper().writeValueAsString(profiled);

    } catch (IOException | JsonLdError e) {
      throw new RuntimeException(e);
    }
  }

  private List<Object> findWebAnnotationsAbout(String uri) {
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
    return webannotations;
  }

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
    Log.info("jsonObject={}", jsonObject);
    Map<Object, Object> context = new HashMap<>();
    JsonLdOptions options = new JsonLdOptions();
    Map<String, Object> compacted = JsonLdProcessor.compact(jsonObject, context, options);
    Log.info("compacted={}", jsonObject);
    Map<String, Object> target = (Map<String, Object>) compacted.get(OA_HAS_TARGET_IRI);
    String resourceRef = "";
    if (target.containsKey("@id")) {
      resourceRef = (String) target.get("@id");
    } else if (target.containsKey(OA_HAS_SOURCE_IRI)) {
      Map<String, Object> source = (Map<String, Object>) target.get(OA_HAS_SOURCE_IRI);
      resourceRef = (String) source.get("@id");
    }
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
