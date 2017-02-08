package nl.knaw.huygens.alexandria.api.w3c;

/*
 * #%L
 * alexandria-api
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
