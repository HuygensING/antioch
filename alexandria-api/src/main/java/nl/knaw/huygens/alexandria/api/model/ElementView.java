package nl.knaw.huygens.alexandria.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_ABSENT)
public class ElementView {
  enum ElementMode {
    show, hide, hideTag
  }

  enum AttributeMode {
    showAll, showOnly, hideAll, hideOnly
  }

  enum AttributeFunction {
    is, isNot, firstOf
  }

  private Optional<ElementView.ElementMode> elementMode;
  private ElementView.AttributeMode attributeMode;
  private List<String> relevantAttributes = new ArrayList<>();
  private AttributePreCondition precondition;

  public void setElementMode(Optional<ElementView.ElementMode> elementMode) {
    this.elementMode = elementMode;
  }

  public Optional<ElementView.ElementMode> getElementMode() {
    return elementMode;
  }

  public void setAttributeMode(ElementView.AttributeMode mode, List<String> parameters) {
    this.attributeMode = mode;
    this.relevantAttributes = parameters;
  }

  public Optional<ElementView.AttributeMode> getAttributeMode() {
    return Optional.ofNullable(attributeMode);
  }

  public void setAttributeMode(ElementView.AttributeMode attributeMode) {
    this.attributeMode = attributeMode;
  }

  public List<String> getRelevantAttributes() {
    return relevantAttributes;
  }

  public void setRelevantAttributes(List<String> relevantAttributes) {
    this.relevantAttributes = relevantAttributes;
  }

  public void setPreCondition(AttributePreCondition precondition) {
    this.precondition = precondition;
  }

  public void setPreCondition(String attribute, AttributeFunction attributeFunction, List<String> parameters) {
    setPreCondition(new AttributePreCondition(attribute, attributeFunction, parameters));
  }

  public Optional<AttributePreCondition> getPreCondition() {
    return Optional.ofNullable(precondition);
  }

}
