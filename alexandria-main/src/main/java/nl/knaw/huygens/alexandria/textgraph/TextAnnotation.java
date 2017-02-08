package nl.knaw.huygens.alexandria.textgraph;

/*
 * #%L
 * alexandria-main
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TextAnnotation {
  public static class Properties {
    public static final String name = "name";
    public static final String attribute_keys = "attribute_keys";
    public static final String attribute_values = "attribute_values";
    public static final String depth = "depth";
    public static final String xmlid = "xml_id";
    public static final String index = "index";
  }

  private String name;
  private Map<String, String> attributes;
  private int depth;
  private Object id;
  private String xmlId;

  public TextAnnotation(String name, Map<String, String> attributes, int depth) {
    this.name = name;
    this.attributes = attributes;
    xmlId = this.attributes.get("xml:id");
    this.depth = depth;
  }

  public void setId(Object object) {
    this.id = object;
  }

  public Object getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getXmlId() {
    return xmlId;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  // TODO:: does this need to be stored?
  public void setDepth(int newDepth) {
    this.depth = newDepth;
  }

  public Integer getDepth() {
    return depth;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  @Override
  public boolean equals(Object other) {
    return EqualsBuilder.reflectionEquals(this, other, false);
  }

}
