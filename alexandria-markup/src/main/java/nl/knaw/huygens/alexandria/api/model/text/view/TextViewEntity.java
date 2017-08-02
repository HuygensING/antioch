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

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.Entity;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;

@JsonTypeName(JsonTypeNames.TEXTVIEW)
@JsonInclude(Include.NON_NULL)
public class TextViewEntity extends JsonWrapperObject implements Entity {
  public String id;

  @JsonProperty(PropertyPrefix.LINK + "xml")
  private URI xmlURI;

  @JsonProperty(PropertyPrefix.LINK + "definition")
  private URI definitionURI;

  public TextViewEntity() {
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setXmlURI(URI xmlURI) {
    this.xmlURI = xmlURI;
  }

  public URI getXmlURI() {
    return this.xmlURI;
  }

  public void setDefinitionURI(URI definitionURI) {
    this.definitionURI = definitionURI;
  }

  public URI getDefinitionURI() {
    return this.definitionURI;
  }

}
