package nl.knaw.huygens.alexandria.api.model;

/*
 * #%L
 * alexandria-api
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BaseLayerDefinition {
  List<BaseElementDefinition> baseElements = new ArrayList<>();
  List<String> subresourceElements = new ArrayList<>();
  UUID baseLayerDefiningResourceId;

  private BaseLayerDefinition() {
  }

  public static BaseLayerDefinition withBaseElements(final BaseElementDefinition... baseElements) {
    return BaseLayerDefinition.withBaseElements(Arrays.asList(baseElements));
  }

  public static BaseLayerDefinition withBaseElements(final List<BaseElementDefinition> baseElements) {
    final BaseLayerDefinition baseLayerDefinition = new BaseLayerDefinition();
    baseLayerDefinition.setBaseElementDefinitions(baseElements);
    return baseLayerDefinition;
  }

  public void setBaseElementDefinitions(final List<BaseElementDefinition> baseElements) {
    this.baseElements = baseElements;
  }

  public void addBaseElementDefinition(final BaseElementDefinition definition) {
    baseElements.add(definition);
  }

  @JsonProperty("baseElements")
  public List<BaseElementDefinition> getBaseElementDefinitions() {
    return baseElements;
  }

  @JsonIgnore
  public UUID getBaseLayerDefiningResourceId() {
    return baseLayerDefiningResourceId;
  }

  public void setBaseLayerDefiningResourceId(UUID baseLayerDefiningResourceId) {
    this.baseLayerDefiningResourceId = baseLayerDefiningResourceId;
  }

  public List<String> getSubresourceElements() {
    return subresourceElements;
  }

  public BaseLayerDefinition setSubresourceElements(List<String> subresourceElements) {
    this.subresourceElements = subresourceElements;
    return this;
  }

}
