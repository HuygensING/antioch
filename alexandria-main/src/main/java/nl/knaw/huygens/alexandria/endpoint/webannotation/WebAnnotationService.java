package nl.knaw.huygens.alexandria.endpoint.webannotation;

import static nl.knaw.huygens.alexandria.api.w3c.WebAnnotationConstants.OA_HAS_SOURCE_IRI;
import static nl.knaw.huygens.alexandria.api.w3c.WebAnnotationConstants.OA_HAS_TARGET_IRI;
import static nl.knaw.huygens.alexandria.api.w3c.WebAnnotationConstants.WEBANNOTATION_TYPE;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.EndpointPaths;
import nl.knaw.huygens.alexandria.api.model.AlexandriaState;
import nl.knaw.huygens.alexandria.api.model.search.AlexandriaQuery;
import nl.knaw.huygens.alexandria.api.model.search.QueryField;
import nl.knaw.huygens.alexandria.api.model.w3c.WebAnnotationPrototype;
import nl.knaw.huygens.alexandria.config.AlexandriaConfiguration;
import nl.knaw.huygens.alexandria.exception.BadRequestException;
import nl.knaw.huygens.alexandria.jaxrs.ThreadContext;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.model.AlexandriaProvenance;
import nl.knaw.huygens.alexandria.model.AlexandriaResource;
import nl.knaw.huygens.alexandria.model.TentativeAlexandriaProvenance;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

public class WebAnnotationService {

  private final AlexandriaService service;
  private final AlexandriaConfiguration config;

  public WebAnnotationService(AlexandriaService service, AlexandriaConfiguration config) {
    this.service = service;
    this.config = config;
  }

  public WebAnnotation validateAndStore(WebAnnotationPrototype prototype) {
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

  URI webAnnotationURI(UUID annotationUUID) {
    return UriBuilder.fromUri(config.getBaseURI())//
        .path(EndpointPaths.WEB_ANNOTATIONS)//
        .path(annotationUUID.toString())//
        .build();
  }

  private String extractResourceRef(Map<String, Object> jsonObject) throws JsonLdError {
    Log.info("jsonObject={}", jsonObject);
    Map<Object, Object> context = new HashMap<>();
    JsonLdOptions options = new JsonLdOptions();
    Map<String, Object> compacted = JsonLdProcessor.compact(jsonObject, context, options);
    Log.info("compacted={}", jsonObject);
    Map<String, Object> target = (Map<String, Object>) compacted.get(OA_HAS_TARGET_IRI);
    String resourceRef = "";
    if (target == null) {
      Log.error("target==null!, compacted={}", compacted); // TODO!
    } else {
      if (target.containsKey("@id")) {
        resourceRef = (String) target.get("@id");
      } else if (target.containsKey(OA_HAS_SOURCE_IRI)) {
        Map<String, Object> source = (Map<String, Object>) target.get(OA_HAS_SOURCE_IRI);
        resourceRef = (String) source.get("@id");
      }
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

    return service.readResource(resourceUUID).get();
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

}
