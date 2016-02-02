package nl.knaw.huygens.alexandria.storage.frames;

/*
 * #%L
 * alexandria-service
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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

import java.util.List;

import nl.knaw.huygens.alexandria.storage.Labels;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

@Vertex(Labels.ANNOTATION)
public abstract class AnnotationVF extends AlexandriaVF {
  public static final String NO_VALUE = ":null";

  // TODO: double-check if (update of) peapod supports outgoing edges with the same label to different types of VF
  // edge labels
  public static final String ANNOTATES_RESOURCE = "annotates_resource";
  public static final String ANNOTATES_ANNOTATION = "annotates_annotation";
  public static final String DEPRECATES = "deprecates";
  public static final String HAS_BODY = "has_body";

  public abstract void setRevision(Integer revision);

  public abstract Integer getRevision();

  public abstract void setLocator(String locatorString);

  public abstract String getLocator();

  @Out
  @Edge(HAS_BODY)
  public abstract AnnotationBodyVF getBody();

  @Out
  @Edge(HAS_BODY)
  public abstract void setBody(AnnotationBodyVF body);

  @Out
  @Edge(ANNOTATES_ANNOTATION)
  public abstract AnnotationVF getAnnotatedAnnotation();

  @Out
  @Edge(ANNOTATES_ANNOTATION)
  public abstract void setAnnotatedAnnotation(AnnotationVF annotationToAnnotate);

  @Out
  @Edge(ANNOTATES_RESOURCE)
  public abstract ResourceVF getAnnotatedResource();

  @Out
  @Edge(ANNOTATES_RESOURCE)
  public abstract void setAnnotatedResource(ResourceVF resourceToAnnotate);

  @In
  @Edge(ANNOTATES_ANNOTATION)
  public abstract List<AnnotationVF> getAnnotatedBy();

  @Out
  @Edge(DEPRECATES)
  public abstract void setDeprecatedAnnotation(AnnotationVF annotationToDeprecate);

  @Out
  @Edge(DEPRECATES)
  public abstract AnnotationVF getDeprecatedAnnotation();

  @In
  @Edge(DEPRECATES)
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
    if (annotatedResource != null && annotatedResource.isSubresource()) {
      return annotatedResource.getUuid();
    }
    return NO_VALUE;
  }

  public ResourceVF getSubResource() {
    ResourceVF annotatedResource = getFirstAnnotatedResource();
    if (annotatedResource != null && annotatedResource.isSubresource()) {
      return annotatedResource;
    }
    return null;
  }

  private ResourceVF getFirstAnnotatedResource() {
    AnnotationVF annotation = findActiveVersionOf(this);
    while (annotation.getAnnotatedResource() == null) {
      annotation = findActiveVersionOf(annotation.getAnnotatedAnnotation());
    }
    return annotation.getAnnotatedResource();
  }

  private AnnotationVF findActiveVersionOf(AnnotationVF annotation) {
    while (annotation.isDeprecated()) {
      annotation = annotation.getDeprecatedBy();
    }
    return annotation;
  }

}
