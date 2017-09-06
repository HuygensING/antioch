package nl.knaw.huygens.antioch.endpoint.webannotation;

/*
 * #%L
 * antioch-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static nl.knaw.huygens.antioch.api.w3c.WebAnnotationConstants.OA_HAS_SOURCE_IRI;
import static nl.knaw.huygens.antioch.api.w3c.WebAnnotationConstants.OA_HAS_TARGET_IRI;
import static nl.knaw.huygens.antioch.api.w3c.WebAnnotationConstants.WEBANNOTATION_TYPE;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.api.model.AntiochState;
import nl.knaw.huygens.antioch.api.model.w3c.WebAnnotationPrototype;
import nl.knaw.huygens.antioch.config.AntiochConfiguration;
import nl.knaw.huygens.antioch.exception.BadRequestException;
import nl.knaw.huygens.antioch.jaxrs.ThreadContext;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.AntiochAnnotationBody;
import nl.knaw.huygens.antioch.model.AntiochProvenance;
import nl.knaw.huygens.antioch.model.AntiochResource;
import nl.knaw.huygens.antioch.model.TentativeAntiochProvenance;
import nl.knaw.huygens.antioch.service.AntiochService;

public class WebAnnotationService {
  private static final Logger LOG = LoggerFactory.getLogger(WebAnnotationService.class);
  private final AntiochService service;
  private final AntiochConfiguration config;
  private final AntiochResourceCache cache = new AntiochResourceCache();

  public WebAnnotationService(AntiochService service, AntiochConfiguration config) {
    this.service = service;
    this.config = config;
  }

  public ObjectNode validateAndStore(ObjectNode annotationNode) {
    try {
      String json = new ObjectMapper().writeValueAsString(annotationNode);

      String resourceRef = extractResourceRef(annotationNode);
      AntiochResource antiochResource = extractAntiochResource(resourceRef);
      AntiochAnnotation annotation = createWebAnnotation(json, antiochResource);

      annotationNode.set("@id", new TextNode(webAnnotationURI(annotation.getId()).toString()));
      return annotationNode;

    } catch (IOException | JsonLdError e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  public WebAnnotation validateAndStore(WebAnnotationPrototype prototype) {
    try {
      String json = new ObjectMapper().writeValueAsString(prototype);
      Map<String, Object> jsonObject = (Map<String, Object>) JsonUtils.fromString(json);

      String resourceRef = extractResourceRef(jsonObject);
      AntiochResource antiochResource = extractAntiochResource(resourceRef);
      AntiochAnnotation annotation = createWebAnnotation(json, antiochResource);

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

  private String extractResourceRef(Object annotationObject) throws JsonLdError {
    try {
      String json = new ObjectMapper().writeValueAsString(annotationObject);
      Object jsonObject = JsonUtils.fromString(json);
      JsonLdOptions options = new JsonLdOptions();
//      LOG.info("annotationObject={}", annotationObject);
      Map<Object, Object> context = new HashMap<>();
      Map<String, Object> compacted = JsonLdProcessor.compact(jsonObject, context, options);
//      LOG.info("compacted={}", compacted);
      Map<String, Object> target = (Map<String, Object>) compacted.get(OA_HAS_TARGET_IRI);
      String resourceRef = "";
      if (target == null) {
        LOG.error("target==null!, compacted={}", compacted); // TODO!
      } else {
        if (target.containsKey("@id")) {
          resourceRef = (String) target.get("@id");
        } else if (target.containsKey(OA_HAS_SOURCE_IRI)) {
          Map<String, Object> source = (Map<String, Object>) target.get(OA_HAS_SOURCE_IRI);
          resourceRef = (String) source.get("@id");
        }
      }
      return resourceRef.replaceFirst("#.*", "");
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private AntiochResource extractAntiochResource(String resourceRef) {
    // first see if there's already a resource with this ref, if so, use this.
    return cache.get(resourceRef).orElseGet(() -> {
      AntiochResource resourceWithRef = service.readResourceWithUniqueRef(resourceRef).orElseGet(() -> {
        UUID resourceUUID = UUID.randomUUID();
        TentativeAntiochProvenance provenance = new TentativeAntiochProvenance(ThreadContext.getUserName(), Instant.now(), AntiochProvenance.DEFAULT_WHY);
        return service.createResource(resourceUUID, resourceRef, provenance, AntiochState.CONFIRMED);
      });
      cache.add(resourceWithRef);
      return resourceWithRef;
    });
  }

  private AntiochAnnotation createWebAnnotation(String json, AntiochResource antiochResource) {
    UUID annotationBodyUUID = UUID.randomUUID();
    // TODO: use information from prototype to create provenance
    TentativeAntiochProvenance annotationProvenance = new TentativeAntiochProvenance(ThreadContext.getUserName(), Instant.now(), AntiochProvenance.DEFAULT_WHY);
    AntiochAnnotationBody annotationBody = service.createAnnotationBody(annotationBodyUUID, WEBANNOTATION_TYPE, json, annotationProvenance);
    AntiochAnnotation annotation = service.annotate(antiochResource, annotationBody, annotationProvenance);
    service.confirmAnnotation(annotation.getId());
    return annotation;
  }

}
