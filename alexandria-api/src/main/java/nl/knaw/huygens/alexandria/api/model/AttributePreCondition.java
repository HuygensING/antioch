package nl.knaw.huygens.alexandria.api.model;

import java.util.List;

import nl.knaw.huygens.alexandria.api.model.ElementView.AttributeFunction;

public class AttributePreCondition {
  private final String attribute;
  private final AttributeFunction function;
  private final List<String> values;

  public AttributePreCondition(final String attribute, final AttributeFunction attributeFunction, final List<String> values) {
    this.attribute = attribute;
    this.function = attributeFunction;
    this.values = values;
  }

  public String getAttribute() {
    return attribute;
  }

  public AttributeFunction getFunction() {
    return function;
  }

  public List<String> getValues() {
    return values;
  }

}
