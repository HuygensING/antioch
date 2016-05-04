package nl.knaw.huygens.alexandria.api.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;

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
      String elementName = entry.getKey();
      validateElementName(elementName);
      validateElementViewDefinition(entry.getValue(), elementName);
    }
  }

  static final Pattern ELEMENTNAME_PATTERN1 = Pattern.compile("[_a-zA-Z].+");
  static final Pattern ELEMENTNAME_PATTERN2 = Pattern.compile("[\\w-\\.]+");

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

  private void validateElementViewDefinition(ElementViewDefinition evd, String elementName) {
    validateAttributeMode(elementName, evd.getAttributeMode());
    validateWhen(elementName, evd.getWhen());
  }

  static final Pattern ATTRIBUTEMODE_SHOWONLY = Pattern.compile("showOnly\\((.*)\\)");
  static final Pattern ATTRIBUTEMODE_HIDEONLY = Pattern.compile("hideOnly\\((.*)\\)");

  private void validateAttributeMode(String elementName, Optional<String> attributeMode) {
    if (attributeMode.isPresent()) {
      String mode = attributeMode.get();
      String prefix = MessageFormat.format("{0}: \"{1}\" ", elementName, mode);

      if ("showAll".equals(mode)) {
        return;
      }

      Matcher matcher2 = ATTRIBUTEMODE_SHOWONLY.matcher(mode);
      if (matcher2.matches()) {
        parseParameters(prefix, matcher2.group(1));
        return;
      }

      if ("hideAll".equals(mode)) {
        return;
      }

      Matcher matcher4 = ATTRIBUTEMODE_HIDEONLY.matcher(mode);
      if (matcher4.matches()) {
        parseParameters(prefix, matcher4.group(1));
        return;
      }

      errors.add(prefix + "is not a valid attributeMode. Valid attributeMode values are: \"showAll\", \"showOnly(attribute,...)\", \"hideAll\", \"hideOnly(attribute,...)\".");
    }
  }

  private void parseParameters(String prefix, String group) {
    List<String> parameters = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(group);
    if (parameters.isEmpty()) {
      errors.add(prefix + "needs one or more attribute names.");
    }
  }

  private void validateWhen(String elementName, Optional<String> optionalWhen) {
    if (optionalWhen.isPresent()) {
      String when = optionalWhen.get();
      String prefix = MessageFormat.format("{0}: \"{1}\" ", elementName, when);

    }
  }

}
