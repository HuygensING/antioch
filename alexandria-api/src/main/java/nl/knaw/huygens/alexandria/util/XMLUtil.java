package nl.knaw.huygens.alexandria.util;

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

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

public class XMLUtil {
  static final Pattern ELEMENTNAME_PATTERN1 = Pattern.compile("[_a-zA-Z].*");
  static final Pattern ELEMENTNAME_PATTERN2 = Pattern.compile("[\\w-\\.:]+");

  public static List<String> validateElementName(final String elementName) {
    List<String> validationErrors = Lists.newArrayList();
    final String prefix = "\"" + elementName + "\" is not a valid element name: element names ";
    if (elementName == null) {
      validationErrors.add("name is missing.");

    } else if (elementName.contains(" ")) {
      validationErrors.add(prefix + "cannot contain spaces.");

    } else if (elementName.toLowerCase().startsWith("xml")) {
      validationErrors.add(prefix + "cannot start with the letters xml (or XML, or Xml, etc).");

    } else if (!ELEMENTNAME_PATTERN1.matcher(elementName).matches()) {
      validationErrors.add(prefix + "must start with a letter or underscore.");

    } else if (!ELEMENTNAME_PATTERN2.matcher(elementName).matches()) {
      validationErrors.add(prefix + "can only contain letters, digits, hyphens, underscores, and periods.");
    }
    return validationErrors;

  }

}
