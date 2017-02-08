package nl.knaw.huygens.alexandria.api.model.text.view;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.Prototype;

@JsonTypeName(JsonTypeNames.TEXTVIEW)
public class TextViewDefinition extends JsonWrapperObject implements Prototype {
  public static final String DEFAULT_ATTRIBUTENAME = ":default";

  private String description = "";

  private Map<String, List<String>> annotationLayers = new HashMap<>();
  private List<String> annotationLayerDepthOrder = new ArrayList<>();

  @JsonProperty("elements")
  private Map<String, ElementViewDefinition> elementViewDefinitions = new LinkedHashMap<>();

  public TextViewDefinition() {
    elementViewDefinitions.clear();
  }

  public TextViewDefinition setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Map<String, ElementViewDefinition> getElementViewDefinitions() {
    return elementViewDefinitions;
  }

  public TextViewDefinition setElementViewDefinitions(Map<String, ElementViewDefinition> elementViewDefinitions) {
    this.elementViewDefinitions = elementViewDefinitions;
    return this;
  }

  public TextViewDefinition setElementViewDefinition(String elementName, ElementViewDefinition elementViewDefinition) {
    this.elementViewDefinitions.put(elementName, elementViewDefinition);
    return this;
  }

  public TextViewDefinition setAnnotationLayers(Map<String, List<String>> annotationLayers) {
    this.annotationLayers = annotationLayers;
    return this;
  }

  public Map<String, List<String>> getAnnotationLayers() {
    return annotationLayers;
  }

  public TextViewDefinition setAnnotationLayerDepthOrder(List<String> annotationLayerDepthOrder) {
    this.annotationLayerDepthOrder = annotationLayerDepthOrder;
    return this;
  }

  public List<String> getAnnotationLayerDepthOrder() {
    return annotationLayerDepthOrder;
  }

  public void substitute(Map<String, String> viewParameters) {
    elementViewDefinitions.values().forEach(elementView -> elementView.substitute(viewParameters));
  }
}
