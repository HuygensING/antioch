package nl.knaw.huygens.alexandria.endpoint.command;

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

import static nl.knaw.huygens.alexandria.text.TextUtil.XML_ID;

import nl.knaw.huygens.alexandria.textgraph.TextAnnotation;

public abstract class TextAnnotationCommand extends ResourcesCommand {

  String getXmlId(TextAnnotation textAnnotation) {
    return textAnnotation.getAttributes().get(XML_ID);
  }

  boolean hasXmlId(TextAnnotation textAnnotation) {
    return textAnnotation.getAttributes().containsKey(XML_ID);
  }

}
