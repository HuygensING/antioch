package nl.knaw.huygens.alexandria.api.w3c;

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
