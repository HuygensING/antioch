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

public class TextView {
  List<ElementDefinition> includedElements = new ArrayList<>();
  List<String> excludedElementTags = new ArrayList<>();
  List<String> ignoredElements = new ArrayList<>();
  UUID textViewDefiningResourceId;

  private TextView() {
  }

  public static TextView withIncludedElements(final ElementDefinition... includedElements) {
    return TextView.withIncludedElements(Arrays.asList(includedElements));
  }

  public static TextView withIncludedElements(final List<ElementDefinition> baseElements) {
    final TextView textView = new TextView();
    textView.setIncludedElementDefinitions(baseElements);
    return textView;
  }

  public void setIncludedElementDefinitions(final List<ElementDefinition> baseElements) {
    this.includedElements = baseElements;
  }

  public void addIncludedElementDefinition(final ElementDefinition definition) {
    includedElements.add(definition);
  }

  @JsonProperty("includedElements")
  public List<ElementDefinition> getIncludedElementDefinitions() {
    return includedElements;
  }

  public TextView setIgnoredElements(List<String> ignoredElements) {
    this.ignoredElements = ignoredElements;
    return this;
  }

  public void addIgnoredElement(final String ignoredTag) {
    ignoredElements.add(ignoredTag);
  }

  public List<String> getIgnoredElements() {
    return ignoredElements;
  }

  public TextView setExcludedElementTags(List<String> excludedElementTags) {
    this.excludedElementTags = excludedElementTags;
    return this;
  }

  public void addExcludedElementTag(final String excludedTag) {
    excludedElementTags.add(excludedTag);
  }

  public List<String> getExcludedElementTags() {
    return excludedElementTags;
  }

  public void setTextViewDefiningResourceId(UUID textViewDefiningResourceId) {
    this.textViewDefiningResourceId = textViewDefiningResourceId;
  }

  @JsonIgnore
  public UUID getTextViewDefiningResourceId() {
    return textViewDefiningResourceId;
  }
}
