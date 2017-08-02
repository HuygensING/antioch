package nl.knaw.huygens.alexandria.api.model;

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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;

@JsonTypeName(JsonTypeNames.ANNOTATOR)
@JsonInclude(Include.NON_EMPTY)
public class Annotator extends JsonWrapperObject {

  private String code = "";
  private String description = "";

  @JsonProperty(PropertyPrefix.LINK + "resource")
  private URI resourceURI;

  public Annotator() {
  }

  public Annotator setCode(String code) {
    this.code = code;
    return this;
  }

  public String getCode() {
    return code;
  }

  public Annotator setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Annotator setResourceURI(URI resourceURI) {
    this.resourceURI = resourceURI;
    return this;
  }

  public URI getResourceURI() {
    return resourceURI;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}
