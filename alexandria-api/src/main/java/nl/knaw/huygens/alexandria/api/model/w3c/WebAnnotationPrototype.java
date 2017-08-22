package nl.knaw.huygens.alexandria.api.model.w3c;

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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Maps;

import java.util.Map;

@JsonPropertyOrder({ "@context", "id", "created", "modified" })
public class WebAnnotationPrototype {
  // for this phase, only accept the json-ld mirador uses.

  // required/fixed part
  private String created;
  private String modified;

  // variable part
  private Map<String, Object> variablePart = Maps.newHashMap();


  public WebAnnotationPrototype setCreated(String created) {
    this.created = created;
    if (this.getModified() == null) {
      this.setModified(this.created);
    }
    return this;
  }

  @JsonProperty("http://purl.org/dc/terms/created")
  public String getCreated() {
    return created;
  }

  public void setModified(String modified) {
    this.modified = modified;
  }

  @JsonProperty("http://purl.org/dc/terms/modified")
  public String getModified() {
    return modified;
  }

  @JsonAnySetter
  public void putKeyValue(String key, Object value) {
    variablePart.put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getVariablePart() {
    return variablePart;
  }

  public void setVariablePart(Map<String, Object> variablePart) {
    this.variablePart = variablePart;
  }

}
