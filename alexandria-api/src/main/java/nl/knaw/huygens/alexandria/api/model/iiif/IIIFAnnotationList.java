package nl.knaw.huygens.alexandria.api.model.iiif;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Maps;

import nl.knaw.huygens.alexandria.api.model.w3c.WebAnnotationPrototype;

@JsonPropertyOrder({ "@context", "resources" })
public class IIIFAnnotationList {

  private List<WebAnnotationPrototype> resources = new ArrayList<>();
  @JsonProperty("@context")
  private String context = "";

  // variable part
  private Map<String, Object> otherProperties = Maps.newHashMap();


  public void setContext(String context) {
    this.context = context;
  }

  public String getContext() {
    return context;
  }

  public void setResources(List<WebAnnotationPrototype> resources) {
    this.resources = resources;
  }

  public List<WebAnnotationPrototype> getResources() {
    return resources;
  }

  @JsonIgnore
  public void setOtherProperties(Map<String, Object> otherProperties) {
    this.otherProperties = otherProperties;
  }

  @JsonAnySetter
  public void putKeyValue(String key, Object value) {
    getOtherProperties().put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getOtherProperties() {
    return otherProperties;
  }
}
