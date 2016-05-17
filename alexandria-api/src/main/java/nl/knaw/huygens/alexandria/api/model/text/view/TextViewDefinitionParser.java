package nl.knaw.huygens.alexandria.api.model.text.view;

import static nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeMode.hideAll;
import static nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeMode.hideOnly;
import static nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeMode.showAll;
import static nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeMode.showOnly;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeFunction;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeMode;

public class TextViewDefinitionParser {

  private final List<String> errors = new ArrayList<>();
  static TextView textView = new TextView();

  public TextViewDefinitionParser(final TextViewDefinition d) {
    textView = new TextView();
    errors.clear();
    parse(d);
  }

  public Optional<TextView> getTextView() {
    return isValid() ? Optional.of(textView) : Optional.empty();
  }

  public boolean isValid() {
    return errors.isEmpty();
  }

  public List<String> getErrors() {
    return errors;
  }

  private void parse(final TextViewDefinition d) {
    textView.setDescription(d.getDescription());
    Map<String, ElementViewDefinition> elementViewDefinitions = d.getElementViewDefinitions();
    elementViewDefinitions.putIfAbsent(TextViewDefinition.DEFAULT_ATTRIBUTENAME, ElementViewDefinition.DEFAULT);
    ElementViewDefinition defaultElementViewDefinition = elementViewDefinitions.get(TextViewDefinition.DEFAULT_ATTRIBUTENAME);
    for (final Entry<String, ElementViewDefinition> entry : elementViewDefinitions.entrySet()) {
      final String elementName = entry.getKey();
      validateElementName(elementName);
      final ElementView elementView = parseElementViewDefinition(elementName, entry.getValue());
      if (elementView.getElementMode() == null) {
        elementView.setElementMode(defaultElementViewDefinition.getElementMode().get());
      }
      if (elementView.getAttributeMode() == null) {
        String modeString = defaultElementViewDefinition.getAttributeMode().get();
        elementView.setAttributeMode(AttributeMode.valueOf(modeString));
      }
      textView.putElementView(elementName, elementView);
    }
  }

  static final Pattern ELEMENTNAME_PATTERN1 = Pattern.compile("[_a-zA-Z].*");
  static final Pattern ELEMENTNAME_PATTERN2 = Pattern.compile("[\\w-\\.:]+");

  private void validateElementName(final String elementName) {
    if (TextViewDefinition.DEFAULT_ATTRIBUTENAME.equals(elementName)) {
      return;
    }
    final String prefix = "\"" + elementName + "\" is not a valid element name: element names ";
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

  private ElementView parseElementViewDefinition(final String elementName, final ElementViewDefinition evd) {
    final ElementView elementView = new ElementView();
    if (evd.getElementMode().isPresent()) {
      elementView.setElementMode(evd.getElementMode().get());
    }
    parseAttributeMode(elementName, evd.getAttributeMode(), elementView);
    parseWhen(elementName, evd.getWhen(), elementView);
    return elementView;
  }

  static final Pattern ATTRIBUTEMODE_SHOWONLY = Pattern.compile(showOnly.name() + "(.*)");
  static final Pattern ATTRIBUTEMODE_HIDEONLY = Pattern.compile(hideOnly.name() + "(.*)");

  private void parseAttributeMode(final String elementName, final Optional<String> attributeMode, final ElementView elementView) {
    if (attributeMode.isPresent()) {
      final String mode = attributeMode.get();
      final String prefix = MessageFormat.format("{0}: \"{1}\" ", elementName, mode);

      if (showAll.name().equals(mode)) {
        elementView.setAttributeMode(showAll, new ArrayList<>());
        return;
      }

      final Matcher matcher2 = ATTRIBUTEMODE_SHOWONLY.matcher(mode);
      if (matcher2.matches()) {
        List<String> parameters = parseParameters(prefix, matcher2.group(1));
        elementView.setAttributeMode(showOnly, parameters);
        return;
      }

      if (hideAll.name().equals(mode)) {
        elementView.setAttributeMode(hideAll, new ArrayList<>());
        return;
      }

      final Matcher matcher4 = ATTRIBUTEMODE_HIDEONLY.matcher(mode);
      if (matcher4.matches()) {
        List<String> parameters = parseParameters(prefix, matcher4.group(1));
        elementView.setAttributeMode(hideOnly, parameters);
        return;
      }

      errors.add(prefix + "is not a valid attributeMode. Valid attributeMode values are: \"showAll\", \"showOnly attribute...\", \"hideAll\", \"hideOnly attribute...\".");
    }
  }

  private List<String> parseParameters(final String prefix, final String group) {
    final List<String> parameters = Splitter.on(" ").trimResults().omitEmptyStrings().splitToList(group);
    if (parameters.isEmpty()) {
      errors.add(prefix + "needs one or more attribute names.");
    }
    return parameters;
  }

  static final Pattern WHEN_PATTERN = Pattern.compile("attribute\\((\\w+)\\)\\.(\\w+)\\((.*)\\)");

  private void parseWhen(final String elementName, final Optional<String> optionalWhen, final ElementView elementView) {
    if (optionalWhen.isPresent()) {
      final String when = optionalWhen.get();
      final String prefix = MessageFormat.format("{0}: \"{1}\" ", elementName, when);
      Matcher matcher = WHEN_PATTERN.matcher(when);
      if (matcher.matches()) {
        String attribute = matcher.group(1);
        String function = matcher.group(2);
        try {
          AttributeFunction attributeFunction = AttributeFunction.valueOf(AttributeFunction.class, function);
          String parameterString = matcher.group(3);
          List<String> parameters = Splitter.on(",")//
              .trimResults(CharMatcher.is('\''))//
              .splitToList(parameterString);
          elementView.setPreCondition(attribute, attributeFunction, parameters);
        } catch (IllegalArgumentException e) {
          addInvalidWhenError(prefix);
        }
        return;
      }

      addInvalidWhenError(prefix);
    }
  }

  private void addInvalidWhenError(final String prefix) {
    errors.add(prefix + "is not a valid condition. Valid 'when' values are: \"attribute(a).is('value')\", \"attribute(a).isNot('value')\", \"attribute(a).firstOf('value0','value1',...)\".");
  }

}
