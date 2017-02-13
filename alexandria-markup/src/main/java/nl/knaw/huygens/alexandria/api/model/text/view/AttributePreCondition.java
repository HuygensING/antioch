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

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeFunction;

public class AttributePreCondition {
  private String attribute;
  private AttributeFunction function;
  private List<String> values = new ArrayList<>();

  public AttributePreCondition() {
  }

  public AttributePreCondition(final String attribute, final AttributeFunction attributeFunction, final List<String> values) {
    this.attribute = attribute;
    this.function = attributeFunction;
    this.values = values;
  }

  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  public String getAttribute() {
    return attribute;
  }

  public void setFunction(AttributeFunction function) {
    this.function = function;
  }

  public AttributeFunction getFunction() {
    return function;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public List<String> getValues() {
    return values;
  }

  @Override
  public String toString() {
    return "attribute(" + attribute + ")." + function.name() + "(" + values.stream().map(v -> "'" + v + "'").collect(joining(",")) + ")";
  }

  public void substitute(Map<String, String> viewParameters) {
    String valueString = Joiner.on(",").join(values);
    valueString = substitute(viewParameters, valueString);
    setValues(Splitter.on(",").splitToList(valueString));
    attribute = substitute(viewParameters, attribute);
  }

  private String substitute(Map<String, String> viewParameters, String valueString) {
    for (Map.Entry<String, String> kv : viewParameters.entrySet()) {
      valueString = valueString.replace("{" + kv.getKey() + "}", kv.getValue().replace("'", ""));
    }
    return valueString;
  }
}
