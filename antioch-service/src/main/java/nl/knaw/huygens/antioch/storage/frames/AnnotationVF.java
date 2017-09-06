package nl.knaw.huygens.antioch.storage.frames;

/*
 * #%L
 * antioch-service
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

import java.util.List;

import nl.knaw.huygens.antioch.storage.VertexLabels;
import peapod.FramedVertex;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(VertexLabels.ANNOTATION)
public abstract class AnnotationVF extends AntiochVF implements FramedVertex<AnnotationVF> {
  public static final String NO_VALUE = ":null";

  public static class EdgeLabels {
    // TODO: double-check if (update of) peapod supports outgoing edges with the same label to different types of VF
    // edge labels
    public static final String ANNOTATES_RESOURCE = "annotates_resource";
    public static final String ANNOTATES_ANNOTATION = "annotates_annotation";
    public static final String DEPRECATES = "deprecates";
    public static final String HAS_BODY = "has_body";
  }

  public abstract void setRevision(Integer revision);

  public abstract Integer getRevision();

  public abstract void setLocator(String locatorString);

  public abstract String getLocator();

  @Out
  @Edge(EdgeLabels.HAS_BODY)
  public abstract AnnotationBodyVF getBody();

  @Out
  @Edge(EdgeLabels.HAS_BODY)
  public abstract void setBody(AnnotationBodyVF body);

  @Out
  @Edge(EdgeLabels.ANNOTATES_ANNOTATION)
  public abstract AnnotationVF getAnnotatedAnnotation();

  @Out
  @Edge(EdgeLabels.ANNOTATES_ANNOTATION)
  public abstract void setAnnotatedAnnotation(AnnotationVF annotationToAnnotate);

  @Out
  @Edge(EdgeLabels.ANNOTATES_RESOURCE)
  public abstract ResourceVF getAnnotatedResource();

  @Out
  @Edge(EdgeLabels.ANNOTATES_RESOURCE)
  public abstract void setAnnotatedResource(ResourceVF resourceToAnnotate);

  @In
  @Edge(EdgeLabels.ANNOTATES_ANNOTATION)
  public abstract List<AnnotationVF> getAnnotatedBy();

  @Out
  @Edge(EdgeLabels.DEPRECATES)
  public abstract void setDeprecatedAnnotation(AnnotationVF annotationToDeprecate);

  @Out
  @Edge(EdgeLabels.DEPRECATES)
  public abstract AnnotationVF getDeprecatedAnnotation();

  @In
  @Edge(EdgeLabels.DEPRECATES)
  public abstract AnnotationVF getDeprecatedBy();

  public String getType() {
    return getBody().getType();
  }

  public String getValue() {
    return getBody().getValue();
  }

  public ResourceVF getResource() {
    ResourceVF annotatedResource = getFirstAnnotatedResource();
    if (annotatedResource != null) {
      ResourceVF parentResource = annotatedResource.getParentResource();
      return parentResource == null ? annotatedResource : parentResource;
    }
    return null;
  }

  public String getResourceId() {
    ResourceVF annotatedResource = getFirstAnnotatedResource();
    if (annotatedResource != null) {
      ResourceVF parentResource = annotatedResource.getParentResource();
      return parentResource == null ? annotatedResource.getUuid() : parentResource.getUuid();
    }
    return NO_VALUE;
  }

  public String getSubResourceId() {
    ResourceVF annotatedResource = getFirstAnnotatedResource();
    if (annotatedResource != null && annotatedResource.isSubResource()) {
      return annotatedResource.getUuid();
    }
    return NO_VALUE;
  }

  public ResourceVF getSubResource() {
    ResourceVF annotatedResource = getFirstAnnotatedResource();
    if (annotatedResource != null && annotatedResource.isSubResource()) {
      return annotatedResource;
    }
    return null;
  }

  // Nullcheck probably necessary due to graph corruption
  private ResourceVF getFirstAnnotatedResource() {
    AnnotationVF annotation = findActiveVersionOf(this);
    while (annotation != null && annotation.getAnnotatedResource() == null) {
      annotation = findActiveVersionOf(annotation.getAnnotatedAnnotation());
    }
    if (annotation == null) {
      return null;
    }
    return annotation.getAnnotatedResource();
  }

  // Nullcheck probably necessary due to graph corruption
  private AnnotationVF findActiveVersionOf(AnnotationVF annotation) {
    if (annotation == null) {
      return annotation;
    }
    while (annotation.isDeprecated()) {
      annotation = annotation.getDeprecatedBy();
    }
    return annotation;
  }

}
