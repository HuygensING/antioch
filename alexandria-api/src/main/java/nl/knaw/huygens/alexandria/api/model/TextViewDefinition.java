package nl.knaw.huygens.alexandria.api.model;

import static java.util.stream.Collectors.toList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

  public List<String> validate() {
    List<String> errors = elementViewDefinitions.values().stream()//
        .map(ElementViewDefinition::validate)//
        .flatMap(List::stream)//
        .collect(toList());
    return errors;
  }

  @JsonIgnore
  public boolean isValid() {
    return validate().isEmpty();
  }

}
