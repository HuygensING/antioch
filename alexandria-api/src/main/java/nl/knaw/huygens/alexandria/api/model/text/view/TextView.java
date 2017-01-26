package nl.knaw.huygens.alexandria.api.model.text.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

  private List<List<String>> orderedLayerTags = new ArrayList<>();

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

  public TextView setOrderedLayerTags(List<List<String>> orderedLayerTags) {
    this.orderedLayerTags = orderedLayerTags;
    return this;
  }

  public List<List<String>> getOrderedLayerTags() {
    return orderedLayerTags;
  }

  public void substitute(Map<String, String> viewParameters) {
    elementViewMap.values().forEach(elementView -> elementView.substitute(viewParameters));
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}
