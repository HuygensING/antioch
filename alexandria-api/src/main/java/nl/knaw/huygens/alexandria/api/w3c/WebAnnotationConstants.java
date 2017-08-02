package nl.knaw.huygens.alexandria.api.w3c;

/*
 * #%L
 * alexandria-api
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

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class WebAnnotationConstants {
  public static final String WEBANNOTATION_TYPE = "alexandria:webannotation";
  public static final String OA_HAS_TARGET_IRI = "http://www.w3.org/ns/oa#hasTarget";
  public static final String OA_HAS_SOURCE_IRI = "http://www.w3.org/ns/oa#hasSource";
  public static final String DEFAULT_PROFILE = "http://www.w3.org/ns/anno.jsonld";
  public static final String IIIF_PROFILE = "http://iiif.io/api/presentation/2/context.json";
  public static final String JSONLD_MEDIATYPE = "application/ld+json";
  public static final String RESOURCE_TYPE_URI = "http://www.w3.org/ns/ldp#Resource";
  public static final String ANNOTATION_TYPE_URI = "http://www.w3.org/ns/oa#Annotation";
  public static final Set<String> ALLOWED_METHODS = ImmutableSet.of("PUT", "GET", "OPTIONS", "HEAD", "DELETE");
}
