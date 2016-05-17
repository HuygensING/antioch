package nl.knaw.huygens.alexandria.api.model.text.view;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TextView {
  private String description = "";

  @JsonProperty("elements")
  private Map<String, ElementView> elementViewMap = new HashMap<>();

  @JsonIgnore
  private UUID textViewDefiningResourceId;

  @JsonIgnore
  private String name;

  public TextView() {
    elementViewMap.clear();
    elementViewMap.put(TextViewDefinition.DEFAULT_ATTRIBUTENAME, ElementView.DEFAULT);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void putElementView(String elementName, ElementView elementView) {
    elementViewMap.put(elementName, elementView);
  }

  public Map<String, ElementView> getElementViewMap() {
    return elementViewMap;
  }

  public TextView setName(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return this.name;
  }

  public void setTextViewDefiningResourceId(UUID uuid) {
    this.textViewDefiningResourceId = uuid;
  }

  public UUID getTextViewDefiningResourceId() {
    return this.textViewDefiningResourceId;
  }

}
