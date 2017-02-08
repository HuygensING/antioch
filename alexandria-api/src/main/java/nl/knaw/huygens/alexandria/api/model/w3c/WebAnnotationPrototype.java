package nl.knaw.huygens.alexandria.api.model.w3c;

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
