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

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeMode;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.ElementMode;

@JsonInclude(Include.NON_ABSENT)
public class ElementViewDefinition {
  public static final ElementViewDefinition DEFAULT = new ElementViewDefinition()//
      .setAttributeMode(AttributeMode.showAll.name())//
      .setElementMode(ElementMode.show);

  // elementMode: (optional, use default settings if absent)
  // # show : show <element> + children
  // # hide : don't show <element> + children
  // # hideTag : don't show <element> tag, show children
  private ElementView.ElementMode elementMode;

  // attributeMode: (optional, use default settings if absent)
  // # showAll : show all attributes
  // # showOnly attribute1 attribute2 ... : show only indicated attributes
  // # hideAll : don't show any attribute
  // # hideOnly attribute1 attribute2 ... : show all attributes except the indicated ones
  private String attributeMode;

  // when (optional, always when 'when' not given)
  // # attribute(rend).is('red')
  // # attribute(rend).isNot('red')
  // # attribute(resp).firstOf('#ed0','#ed1','')
  private String when;

  public Optional<ElementView.ElementMode> getElementMode() {
    return Optional.ofNullable(elementMode);
  }

  public ElementViewDefinition setElementMode(ElementView.ElementMode elementMode) {
    this.elementMode = elementMode;
    return this;
  }

  public Optional<String> getAttributeMode() {
    return Optional.ofNullable(attributeMode);
  }

  public ElementViewDefinition setAttributeMode(String attributeMode) {
    this.attributeMode = attributeMode;
    return this;
  }

  public Optional<String> getWhen() {
    return Optional.ofNullable(when);
  }

  public ElementViewDefinition setWhen(String when) {
    this.when = when;
    return this;
  }

  public void substitute(Map<String, String> viewParameters) {
    if (StringUtils.isNotEmpty(when)) {
      for (Map.Entry<String, String> kv : viewParameters.entrySet()) {
        when = when.replace("{" + kv.getKey() + "}", kv.getValue());
      }
    }
  }

}
