package nl.knaw.huygens.antioch.util;

/*
 * #%L
 * antioch-api
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

public class XMLUtil {
  private static final Pattern ELEMENTNAME_PATTERN1 = Pattern.compile("[_a-zA-Z].*");
  private static final Pattern ELEMENTNAME_PATTERN2 = Pattern.compile("[\\w-\\.:]+");

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
