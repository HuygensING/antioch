package nl.knaw.huygens.alexandria.api.model.text.view;

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
import java.util.Objects;

public class ElementDefinition {
  String name = "";
  List<String> includedAttributes = new ArrayList<>();
  String condition = "";

  private ElementDefinition() {
  }

  public static ElementDefinition withName(final String name) {
    ElementDefinition baseElementDefinition = new ElementDefinition();
    baseElementDefinition.setName(name);
    return baseElementDefinition;
  }

  public ElementDefinition withAttributes(final String... attributes) {
    setIncludedAttributes(Arrays.asList(attributes));
    return this;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public void setIncludedAttributes(final List<String> includedAttributes) {
    this.includedAttributes = includedAttributes;
  }

  public List<String> getIncludedAttributes() {
    return includedAttributes;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, includedAttributes);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof ElementDefinition //
        && Objects.equals(this.getName(), ((ElementDefinition) other).getName())//
        && Objects.equals(this.getIncludedAttributes(), ((ElementDefinition) other).getIncludedAttributes());
  }

}
