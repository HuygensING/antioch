package nl.knaw.huygens.alexandria.api.model.text.view;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.Prototype;

@JsonTypeName(JsonTypeNames.TEXTVIEW)
public class TextViewDefinition extends JsonWrapperObject implements Prototype {
  public static final String DEFAULT_ATTRIBUTENAME = ":default";

  private String description = "";

  @JsonProperty("elements")
  private Map<String, ElementViewDefinition> elementViewDefinitions = new LinkedHashMap<>();

  public TextViewDefinition() {
    elementViewDefinitions.clear();
  }

  public TextViewDefinition setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Map<String, ElementViewDefinition> getElementViewDefinitions() {
    return elementViewDefinitions;
  }

  public void setElementViewDefinitions(Map<String, ElementViewDefinition> elementViewDefinitions) {
    this.elementViewDefinitions = elementViewDefinitions;
  }

  public void setElementViewDefinition(String elementName, ElementViewDefinition elementViewDefinition) {
    this.elementViewDefinitions.put(elementName, elementViewDefinition);
  }

}
