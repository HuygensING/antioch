package nl.knaw.huygens.alexandria.api.model.text.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonInclude(Include.NON_ABSENT)
public class ElementView {

  public enum ElementMode {
    show, hide, hideTag
  }

  public enum AttributeMode {
    showAll, showOnly, hideAll, hideOnly
  }

  public enum AttributeFunction {
    is, isNot, firstOf
  }

  public static final ElementView DEFAULT = new ElementView()//
    .setElementMode(ElementMode.show)//
    .setAttributeMode(AttributeMode.showAll);

  private ElementView.ElementMode elementMode;
  private ElementView.AttributeMode attributeMode;
  private List<String> relevantAttributes = new ArrayList<>();
  private AttributePreCondition precondition;

  public ElementView setElementMode(ElementView.ElementMode elementMode) {
    this.elementMode = elementMode;
    return this;
  }

  public ElementView.ElementMode getElementMode() {
    return elementMode;
  }

  public void setAttributeMode(ElementView.AttributeMode mode, List<String> parameters) {
    this.attributeMode = mode;
    this.relevantAttributes = parameters;
  }

  public ElementView.AttributeMode getAttributeMode() {
    return attributeMode;
  }

  public ElementView setAttributeMode(ElementView.AttributeMode attributeMode) {
    this.attributeMode = attributeMode;
    return this;
  }

  public List<String> getRelevantAttributes() {
    return relevantAttributes;
  }

  public ElementView setRelevantAttributes(List<String> relevantAttributes) {
    this.relevantAttributes = relevantAttributes;
    return this;
  }

  public void setPreCondition(AttributePreCondition precondition) {
    this.precondition = precondition;
  }

  public ElementView setPreCondition(String attribute, AttributeFunction attributeFunction, List<String> parameters) {
    setPreCondition(new AttributePreCondition(attribute, attributeFunction, parameters));
    return this;
  }

  public Optional<AttributePreCondition> getPreCondition() {
    return Optional.ofNullable(precondition);
  }

  public void substitute(Map<String, String> viewParameters) {
    getPreCondition().ifPresent(preconditions -> precondition.substitute(viewParameters));
  }

  @Override
  public String toString() {
    return "elementMode:" + elementMode.name() + ", attributeMode:" + attributeMode.name() + ", when:" + precondition;
  }
}
