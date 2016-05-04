package nl.knaw.huygens.alexandria.api.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("textView")
public class TextViewDefinition extends JsonWrapperObject implements Prototype {
  public static final String DEFAULT = ":default";
  private String description = "";
  @JsonProperty("elements")
  private Map<String, ElementViewDefinition> elementViewDefinitions = new LinkedHashMap<>();

  public void setDescription(String description) {
    this.description = description;
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

}
