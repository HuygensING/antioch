package nl.knaw.huygens.alexandria.endpoint.annotation;

import java.net.URI;

/*
 * #%L
 * alexandria-main
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;
import nl.knaw.huygens.alexandria.endpoint.AbstractAnnotatableEntity;
import nl.knaw.huygens.alexandria.endpoint.LocationBuilder;
import nl.knaw.huygens.alexandria.model.AbstractAnnotatable;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.IdentifiablePointer;
import nl.knaw.huygens.alexandria.textlocator.AlexandriaTextLocator;

@JsonTypeName("annotation")
@JsonPropertyOrder({ "id", "revision", "state", "type", "value" })
@JsonInclude(Include.NON_NULL)
@ApiModel("annotation")
public class AnnotationEntity extends AbstractAnnotatableEntity {

  @JsonIgnore
  private final AlexandriaAnnotation annotation;

  public static AnnotationEntity of(AlexandriaAnnotation someAnnotation) {
    return new AnnotationEntity(someAnnotation);
  }

  private AnnotationEntity(AlexandriaAnnotation annotation) {
    this.annotation = annotation;
  }

  public final AnnotationEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  public String getLocator() {
    AlexandriaTextLocator locator = annotation.getLocator();
    if (locator != null) {
      return locator.toString();
    }
    return null;
  }

  public String getType() {
    return annotation.getBody().getType();
  }

  public String getValue() {
    return annotation.getBody().getValue();
  }

  public Integer getRevision() {
    return annotation.getRevision();
  }

  @JsonProperty(PropertyPrefix.LINK + "annotates")
  public String getAnnotates() {
    IdentifiablePointer<?> annotatablePointer = annotation.getAnnotatablePointer();
    return annotatablePointer == null ? "" : locationBuilder.locationOf(annotatablePointer).toString();
  }

  @JsonProperty(PropertyPrefix.LINK + "deprecates")
  public String getDeprecates() {
    return annotation.getRevision() > 0//
        ? locationBuilder.locationOf(annotation, AnnotationsEndpoint.REVPATH, String.valueOf(annotation.getRevision() - 1)).toString()//
        : "";
  }

  @JsonProperty(PropertyPrefix.LINK + "versioned_self")
  public URI getVersionURL() {
    return locationBuilder.locationOf(annotation, AnnotationsEndpoint.REVPATH, String.valueOf(annotation.getRevision()));
  }

  @JsonProperty(PropertyPrefix.LINK + "current_version")
  public URI getCurrentVersionURL() {
    return locationBuilder.locationOf(annotation);
  }

  @Override
  protected AbstractAnnotatable getAnnotatable() {
    return annotation;
  }

}
