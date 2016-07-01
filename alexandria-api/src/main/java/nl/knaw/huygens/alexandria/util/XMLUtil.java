package nl.knaw.huygens.alexandria.util;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

public class XMLUtil {
  static final Pattern ELEMENTNAME_PATTERN1 = Pattern.compile("[_a-zA-Z].*");
  static final Pattern ELEMENTNAME_PATTERN2 = Pattern.compile("[\\w-\\.:]+");

  public static List<String> validateElementName(final String elementName) {
    List<String> validationErrors = Lists.newArrayList();
    final String prefix = "\"" + elementName + "\" is not a valid element name: element names ";
    if (elementName.contains(" ")) {
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
