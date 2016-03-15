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
import java.util.Objects;

public class BaseElementDefinition {
  String name = "";
  List<String> baseAttributes = new ArrayList<>();

  private BaseElementDefinition() {
  }

  public static BaseElementDefinition withName(final String name) {
    BaseElementDefinition baseElementDefinition = new BaseElementDefinition();
    baseElementDefinition.setName(name);
    return baseElementDefinition;
  }

  public BaseElementDefinition withAttributes(final String... attributes) {
    setBaseAttributes(Arrays.asList(attributes));
    return this;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setBaseAttributes(final List<String> baseAttributes) {
    this.baseAttributes = baseAttributes;
  }

  public List<String> getBaseAttributes() {
    return baseAttributes;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, baseAttributes);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof BaseElementDefinition //
        && Objects.equals(this.getName(), ((BaseElementDefinition) other).getName())//
        && Objects.equals(this.getBaseAttributes(), ((BaseElementDefinition) other).getBaseAttributes());
  }

}
