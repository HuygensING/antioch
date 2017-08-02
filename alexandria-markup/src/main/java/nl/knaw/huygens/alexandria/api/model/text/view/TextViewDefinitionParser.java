package nl.knaw.huygens.alexandria.api.model.text.view;

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

import static java.util.stream.Collectors.toList;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeFunction;
import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeMode;
import nl.knaw.huygens.alexandria.util.XMLUtil;

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

  private void parse(final TextViewDefinition textViewDefinition) {
    textView.setDescription(textViewDefinition.getDescription());

    List<List<String>> parseAnnotationLayers = parseAnnotationLayers(textViewDefinition.getAnnotationLayers(), textViewDefinition.getAnnotationLayerDepthOrder());
    textView.setOrderedLayerTags(parseAnnotationLayers);

    Map<String, ElementViewDefinition> elementViewDefinitions = textViewDefinition.getElementViewDefinitions();
    elementViewDefinitions.putIfAbsent(TextViewDefinition.DEFAULT_ATTRIBUTENAME, ElementViewDefinition.DEFAULT);
    ElementViewDefinition defaultElementViewDefinition = elementViewDefinitions.get(TextViewDefinition.DEFAULT_ATTRIBUTENAME);
    for (final Entry<String, ElementViewDefinition> entry : elementViewDefinitions.entrySet()) {
      final String elementName = entry.getKey();
      if (!TextViewDefinition.DEFAULT_ATTRIBUTENAME.equals(elementName)) {
        errors.addAll(XMLUtil.validateElementName(elementName));
      }

      final ElementView elementView = parseElementViewDefinition(elementName, entry.getValue());
      if (elementView.getElementMode() == null) {
        elementView.setElementMode(defaultElementViewDefinition.getElementMode()//
            .orElse(ElementViewDefinition.DEFAULT.getElementMode().get()));
      }
      if (elementView.getAttributeMode() == null) {
        String modeString = defaultElementViewDefinition.getAttributeMode()//
            .orElse(ElementViewDefinition.DEFAULT.getAttributeMode().get());
        elementView.setAttributeMode(AttributeMode.valueOf(modeString));
      }
      textView.putElementView(elementName, elementView);
    }
  }

  private List<List<String>> parseAnnotationLayers(Map<String, List<String>> annotationLayers, List<String> annotationLayerDepthOrder) {
    Set<String> definedLayers = annotationLayers.keySet();
    annotationLayerDepthOrder.forEach(layerName -> {
      if (!definedLayers.contains(layerName)) {
        errors.add("annotationLayerDepthOrder: " + layerName + " not defined in annotationLayers");
      }
    });
    List<List<String>> list = annotationLayerDepthOrder.stream()//
        .map(annotationLayers::get)//
        .collect(toList());
    return list;
  }

  private ElementView parseElementViewDefinition(final String elementName, final ElementViewDefinition evd) {
    final ElementView elementView = new ElementView();
    evd.getElementMode().ifPresent(elementView::setElementMode);
    parseAttributeMode(elementName, evd.getAttributeMode(), elementView);
    parseWhen(elementName, evd.getWhen(), elementView);
    return elementView;
  }

  static final Pattern ATTRIBUTEMODE_SHOWONLY = Pattern.compile(showOnly.name() + "(.*)");
  static final Pattern ATTRIBUTEMODE_HIDEONLY = Pattern.compile(hideOnly.name() + "(.*)");

  private void parseAttributeMode(final String elementName, final Optional<String> attributeMode, final ElementView elementView) {
    attributeMode.ifPresent(mode -> {
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
    });
  }

  private List<String> parseParameters(final String prefix, final String group) {
    final List<String> parameters = Splitter.on(" ").trimResults().omitEmptyStrings().splitToList(group);
    if (parameters.isEmpty()) {
      errors.add(prefix + "needs one or more attribute names.");
    }
    return parameters;
  }

  static final Pattern WHEN_PATTERN = Pattern.compile("attribute\\((\\w+|\\{\\w+\\})\\)\\.(\\w+)\\((.*)\\)");

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
