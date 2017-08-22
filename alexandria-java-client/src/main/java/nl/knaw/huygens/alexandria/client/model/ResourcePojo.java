package nl.knaw.huygens.alexandria.client.model;

import java.net.URI;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * #%L
 * alexandria-java-client
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

import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;

@JsonTypeName(JsonTypeNames.RESOURCE)
public class ResourcePojo extends AbstractAccountablePojo<ResourcePojo> {
  private String id;
  private String ref;
  private StatePojo state;

  @JsonProperty(PropertyPrefix.LINK + "annotations")
  List<URI> annotationURIs;

  @JsonProperty(PropertyPrefix.LINK + "provenance")
  URI provenanceURI;

  @JsonProperty(PropertyPrefix.LINK + "subresources")
  List<URI> subresourceURIs;

  @JsonProperty(PropertyPrefix.LINK + "text")
  URI textURI;

  public ResourcePojo() {
  }

  public ResourcePojo(final String ref) {
    this.setRef(ref);
  }

  public URI getTextURI() {
    return textURI;
  }

  public void setTextURI(final URI textURI) {
    this.textURI = textURI;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getRef() {
    return ref;
  }

  public void setRef(final String ref) {
    this.ref = ref;
  }

  public List<URI> getAnnotationURIs() {
    return annotationURIs;
  }

  public void setAnnotationURIs(final List<URI> annotationURIs) {
    this.annotationURIs = annotationURIs;
  }

  public URI getProvenanceURI() {
    return provenanceURI;
  }

  public void setProvenanceURI(final URI provenanceURI) {
    this.provenanceURI = provenanceURI;
  }

  public List<URI> getSubresourceURIs() {
    return subresourceURIs;
  }

  public void setSubresourceURIs(final List<URI> subresourceURIs) {
    this.subresourceURIs = subresourceURIs;
  }

  public StatePojo getState() {
    return state;
  }

  public void setState(final StatePojo state) {
    this.state = state;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}
