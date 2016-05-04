package nl.knaw.huygens.alexandria.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class TextViewDefinitionParser {
  private List<String> errors = new ArrayList<>();

  public TextViewDefinitionParser(TextViewDefinition d) {
    validate(d);
  }

  public boolean isValid() {
    return errors.isEmpty();
  }

  public List<String> getErrors() {
    return errors;
  }

  private void validate(TextViewDefinition d) {
    for (Entry<String, ElementViewDefinition> entry : d.getElementViewDefinitions().entrySet()) {
      validateElementName(entry.getKey());
      validateElementViewDefinition(entry.getValue());
    }
  }

  Pattern ELEMENTNAME_PATTERN1 = Pattern.compile("[_a-zA-Z].+");
  Pattern ELEMENTNAME_PATTERN2 = Pattern.compile("[\\w-\\.]+");

  private void validateElementName(String elementName) {
    if (TextViewDefinition.DEFAULT.equals(elementName)) {
      return;
    }
    String prefix = "\"" + elementName + "\" is not a valid element name: element names ";
    if (elementName.contains(" ")) {
      errors.add(prefix + "cannot contain spaces.");

    } else if (elementName.toLowerCase().startsWith("xml")) {
      errors.add(prefix + "cannot start with the letters xml (or XML, or Xml, etc).");

    } else if (!ELEMENTNAME_PATTERN1.matcher(elementName).matches()) {
      errors.add(prefix + "must start with a letter or underscore.");

    } else if (!ELEMENTNAME_PATTERN2.matcher(elementName).matches()) {
      errors.add(prefix + "can only contain letters, digits, hyphens, underscores, and periods.");
    }
  }

  private void validateElementViewDefinition(ElementViewDefinition evd) {

  }

}
