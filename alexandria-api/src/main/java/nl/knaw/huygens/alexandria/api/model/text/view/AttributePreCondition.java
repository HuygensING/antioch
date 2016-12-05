package nl.knaw.huygens.alexandria.api.model.text.view;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

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
    return "attribute(" + attribute + ")." + function.name() + "(" + Joiner.on(",").join(values) + ")";
  }

}
