package nl.knaw.huygens.alexandria.text;

/*
 * #%L
 * alexandria-main
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

import java.text.MessageFormat;
import java.util.stream.Collectors;

import nl.knaw.huygens.tei.Element;

public class AnnotationData {
  String annotatedBaseText = "";
  XmlAnnotationLevel level = XmlAnnotationLevel.element;
  String type = "";
  String xpath = "";
  Object value;

  public String getAnnotatedBaseText() {
    return annotatedBaseText;
  }

  public AnnotationData setAnnotatedBaseText(String annotatedBaseText) {
    this.annotatedBaseText = annotatedBaseText;
    return this;
  }

  public XmlAnnotationLevel getLevel() {
    return level;
  }

  public AnnotationData setLevel(XmlAnnotationLevel level) {
    this.level = level;
    return this;
  }

  public String getXPath() {
    return xpath;
  }

  public AnnotationData setXPath(String xpath) {
    this.xpath = xpath;
    return this;
  }

  public String getType() {
    return type;
  }

  public AnnotationData setType(String type) {
    this.type = type;
    return this;
  }

  public Object getValue() {
    return value;
  }

  public AnnotationData setValue(Object value) {
    this.value = value;
    return this;
  }

  public String toVerbose() {
    if (getLevel().equals(XmlAnnotationLevel.element)) {
      Element element = (Element) value;
      String prefix = ", with ";
      String attributeAnnotationActions = element.getAttributes().entrySet().stream()//
          .map(kv -> "attribute annotation '" + kv.getKey() + "'='" + kv.getValue() + "'")//
          .collect(Collectors.joining(", and ", prefix, ""));

      if (attributeAnnotationActions.equals(prefix)) {
        attributeAnnotationActions = "";
      }

      return (MessageFormat.format(//
          "adding element annotation on text ''{0}'', on xpath ''{1}'' element={2}{3}", //
          annotatedBaseText, //
          xpath, //
          type, //
          attributeAnnotationActions)//
      );
    }

    return "adding attribute annotation on xpath '" + xpath + "' : " + type + "=" + value;
  }

}
