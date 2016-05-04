package nl.knaw.huygens.alexandria.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElementView {
  enum ElementMode {
    show, hide, hideTag
  }

  enum AttributeMode {
    showAll, showOnly, hideAll, hideOnly
  }

  private ElementView.AttributeMode attributeMode;
  private List<String> relevantAttributes = new ArrayList<>();
  private Optional<ElementView.ElementMode> elementMode;

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

}
