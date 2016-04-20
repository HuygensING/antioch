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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("textView")
public class TextViewPrototype extends JsonWrapperObject implements Prototype {
  private String description = "";
  private List<ElementDefinition> includedElements = new ArrayList<>();
  private List<String> excludedElementTags = new ArrayList<>();
  private List<String> ignoredElements = new ArrayList<>();

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public TextViewPrototype setIncludedElements(List<ElementDefinition> includedElements) {
    this.includedElements = includedElements;
    return this;
  }

  public List<ElementDefinition> getIncludedElements() {
    return includedElements;
  }

  public void setExcludedElements(List<String> excludedElementTags) {
    this.excludedElementTags = excludedElementTags;
  }

  public List<String> getExcludedElementTags() {
    return excludedElementTags;
  }

  public TextViewPrototype setIgnoredElements(List<String> ignoredElements) {
    this.ignoredElements = ignoredElements;
    return this;
  }

  public List<String> getIgnoredElements() {
    return ignoredElements;
  }

  @JsonIgnore
  public boolean isValid() {
    // defining both includedElements and excludedElements makes no sense
    return includedElements.isEmpty() || excludedElementTags.isEmpty();
  }
}
