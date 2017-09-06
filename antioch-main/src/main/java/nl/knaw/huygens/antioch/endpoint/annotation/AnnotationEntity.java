package nl.knaw.huygens.antioch.endpoint.annotation;

import java.net.URI;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import nl.knaw.huygens.antioch.api.EndpointPaths;
import nl.knaw.huygens.antioch.api.JsonTypeNames;
import nl.knaw.huygens.antioch.api.model.PropertyPrefix;
import nl.knaw.huygens.antioch.endpoint.AbstractAnnotatableEntity;
import nl.knaw.huygens.antioch.endpoint.LocationBuilder;
import nl.knaw.huygens.antioch.model.AbstractAnnotatable;
import nl.knaw.huygens.antioch.model.AntiochAnnotation;
import nl.knaw.huygens.antioch.model.IdentifiablePointer;
import nl.knaw.huygens.antioch.textlocator.AntiochTextLocator;

@JsonTypeName(JsonTypeNames.ANNOTATION)
@JsonPropertyOrder({ "id", "revision", "state", "type", "value" })
@JsonInclude(Include.NON_NULL)
@ApiModel(JsonTypeNames.ANNOTATION)
public class AnnotationEntity extends AbstractAnnotatableEntity {

  @JsonIgnore
  private final AntiochAnnotation annotation;

  public static AnnotationEntity of(AntiochAnnotation someAnnotation) {
    return new AnnotationEntity(someAnnotation);
  }

  private AnnotationEntity(AntiochAnnotation annotation) {
    this.annotation = annotation;
  }

  public final AnnotationEntity withLocationBuilder(LocationBuilder locationBuilder) {
    this.locationBuilder = locationBuilder;
    return this;
  }

  public String getLocator() {
    AntiochTextLocator locator = annotation.getLocator();
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
        ? locationBuilder.locationOf(annotation, EndpointPaths.REV, String.valueOf(annotation.getRevision() - 1)).toString()//
        : "";
  }

  @JsonProperty(PropertyPrefix.LINK + "versioned_self")
  public URI getVersionURL() {
    return locationBuilder.locationOf(annotation, EndpointPaths.REV, String.valueOf(annotation.getRevision()));
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
