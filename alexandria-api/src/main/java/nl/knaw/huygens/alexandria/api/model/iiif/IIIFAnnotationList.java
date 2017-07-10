package nl.knaw.huygens.alexandria.api.model.iiif;

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
