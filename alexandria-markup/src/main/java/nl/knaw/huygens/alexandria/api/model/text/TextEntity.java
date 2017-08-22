package nl.knaw.huygens.alexandria.api.model.text;

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
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.Entity;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewEntity;

@JsonTypeName(JsonTypeNames.RESOURCETEXT)
@JsonInclude(Include.NON_NULL)
public class TextEntity extends JsonWrapperObject implements Entity {

  @JsonProperty("views")
  private List<TextViewEntity> textViews = Lists.newArrayList();

  @JsonProperty(PropertyPrefix.LINK + "xml")
  private URI xmlURI;

  @JsonProperty(PropertyPrefix.LINK + "dot")
  private URI dotURI;

  public TextEntity() {
  }

  public void setTextViews(List<TextViewEntity> textViews) {
    this.textViews = textViews;
  }

  public List<TextViewEntity> getTextViews() {
    return textViews;
  }

  public void setXmlURI(URI xmlURI) {
    this.xmlURI = xmlURI;
  }

  public URI getXmlURI() {
    return this.xmlURI;
  }

  public void setDotURI(URI dotURI) {
    this.dotURI = dotURI;
  }

  public URI getDotURI() {
    return this.dotURI;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}
