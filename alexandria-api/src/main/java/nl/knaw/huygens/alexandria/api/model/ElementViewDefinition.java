package nl.knaw.huygens.alexandria.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ElementViewDefinition {
  enum ElementMode {
    show, hide, hideTag
  }

  // # elementMode: (optional, use default settings if absent)
  // # show : show <element> + children
  // # hide : don't show <element> + children
  // # hideTag : don't show <element> tag, show children
  private ElementMode elementMode = ElementMode.show;

  // # attributeMode: (optional, use default settings if absent)
  // # showAll : show all attributes
  // # showOnly([attribute1, ...]) : show only indicated attributes
  // # hideAll : don't show any attribute
  // # hideOnly([attribute1, ...]) : show all attributes except the indicated ones
  private String attributeMode;

  // # when (optional, always when 'when' not given)
  // # attribute(rend).is('red')
  // # attribute(rend).isNot('red')
  // # attribute(resp).firstOf('#ed0','#ed1','')
  private String when;

  public ElementMode getElementMode() {
    return elementMode;
  }

  public ElementViewDefinition setElementMode(ElementMode elementMode) {
    this.elementMode = elementMode;
    return this;
  }

  public String getAttributeMode() {
    return attributeMode;
  }

  public ElementViewDefinition setAttributeMode(String attributeMode) {
    this.attributeMode = attributeMode;
    return this;
  }

  public String getWhen() {
    return when;
  }

  public ElementViewDefinition setWhen(String when) {
    this.when = when;
    return this;
  }

}
