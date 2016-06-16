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

import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;

@JsonTypeName(JsonTypeNames.SUBRESOURCE)
public class SubResourcePojo extends AbstractAccountablePojo<SubResourcePojo> {
  private String id;
  private String sub;
  private StatePojo state;

  @JsonProperty(PropertyPrefix.LINK + "annotations")
  List<URI> annotationURIs;

  @JsonProperty(PropertyPrefix.LINK + "provenance")
  URI provenanceURI;

  @JsonProperty(PropertyPrefix.LINK + "partOf")
  URI parentURI;

  @JsonProperty(PropertyPrefix.LINK + "subresources")
  List<URI> subresourceURIs;

  @JsonProperty(PropertyPrefix.LINK + "text")
  URI textURI;

  public SubResourcePojo() {
  }

  public SubResourcePojo(final String sub) {
    this.setSub(sub);
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

  public String getSub() {
    return sub;
  }

  public void setSub(final String ref) {
    this.sub = ref;
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

  public URI getParentURI() {
    return parentURI;
  }

  public void setParentURI(final URI parentURI) {
    this.parentURI = parentURI;
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
