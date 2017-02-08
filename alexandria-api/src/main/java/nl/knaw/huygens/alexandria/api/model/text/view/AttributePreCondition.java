package nl.knaw.huygens.alexandria.api.model.text.view;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import nl.knaw.huygens.alexandria.api.model.text.view.ElementView.AttributeFunction;

public class AttributePreCondition {
  private String attribute;
  private AttributeFunction function;
  private List<String> values = new ArrayList<>();

  public AttributePreCondition() {
  }

  public AttributePreCondition(final String attribute, final AttributeFunction attributeFunction, final List<String> values) {
    this.attribute = attribute;
    this.function = attributeFunction;
    this.values = values;
  }

  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  public String getAttribute() {
    return attribute;
  }

  public void setFunction(AttributeFunction function) {
    this.function = function;
  }

  public AttributeFunction getFunction() {
    return function;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public List<String> getValues() {
    return values;
  }

  @Override
  public String toString() {
    return "attribute(" + attribute + ")." + function.name() + "(" + values.stream().map(v -> "'" + v + "'").collect(joining(",")) + ")";
  }

  public void substitute(Map<String, String> viewParameters) {
    String valueString = Joiner.on(",").join(values);
    valueString = substitute(viewParameters, valueString);
    setValues(Splitter.on(",").splitToList(valueString));
    attribute = substitute(viewParameters, attribute);
  }

  private String substitute(Map<String, String> viewParameters, String valueString) {
    for (Map.Entry<String, String> kv : viewParameters.entrySet()) {
      valueString = valueString.replace("{" + kv.getKey() + "}", kv.getValue().replace("'", ""));
    }
    return valueString;
  }
}
