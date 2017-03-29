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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

@JsonInclude(Include.NON_ABSENT)
public class ElementView {

  public enum ElementMode {
    show, hide, hideTag, asMilestones
  }

  public enum AttributeMode {
    showAll, showOnly, hideAll, hideOnly
  }

  public enum AttributeFunction {
    is, isNot, firstOf
  }

  public static final ElementView DEFAULT = new ElementView()//
    .setElementMode(ElementMode.show)//
    .setAttributeMode(AttributeMode.showAll);

  private ElementView.ElementMode elementMode;
  private ElementView.AttributeMode attributeMode;
  private List<String> relevantAttributes = new ArrayList<>();
  private AttributePreCondition precondition;

  public ElementView setElementMode(ElementView.ElementMode elementMode) {
    this.elementMode = elementMode;
    return this;
  }

  public ElementView.ElementMode getElementMode() {
    return elementMode;
  }

  public void setAttributeMode(ElementView.AttributeMode mode, List<String> parameters) {
    this.attributeMode = mode;
    this.relevantAttributes = parameters;
  }

  public ElementView.AttributeMode getAttributeMode() {
    return attributeMode;
  }

  public ElementView setAttributeMode(ElementView.AttributeMode attributeMode) {
    this.attributeMode = attributeMode;
    return this;
  }

  public List<String> getRelevantAttributes() {
    return relevantAttributes;
  }

  public ElementView setRelevantAttributes(List<String> relevantAttributes) {
    this.relevantAttributes = relevantAttributes;
    return this;
  }

  public void setPreCondition(AttributePreCondition precondition) {
    this.precondition = precondition;
  }

  public ElementView setPreCondition(String attribute, AttributeFunction attributeFunction, List<String> parameters) {
    setPreCondition(new AttributePreCondition(attribute, attributeFunction, parameters));
    return this;
  }

  public Optional<AttributePreCondition> getPreCondition() {
    return Optional.ofNullable(precondition);
  }

  public void substitute(Map<String, String> viewParameters) {
    String relevantAttributesString = Joiner.on(",").join(relevantAttributes);
    for (Map.Entry<String, String> kv : viewParameters.entrySet()) {
      relevantAttributesString = relevantAttributesString.replace("{" + kv.getKey() + "}", kv.getValue().replace("'", ""));
    }
    setRelevantAttributes(Splitter.on(",").splitToList(relevantAttributesString));
    getPreCondition().ifPresent(precondition -> precondition.substitute(viewParameters));
  }

  @Override
  public String toString() {
    return "elementMode:" + elementMode.name() + ", attributeMode:" + attributeMode.name() + ", when:" + precondition;
  }
}
