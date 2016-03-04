package nl.knaw.huygens.alexandria.api.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BaseLayerDefinition {
  List<BaseElementDefinition> baseElements = new ArrayList<>();
  List<String> subresourceElements = new ArrayList<>();
  UUID baseLayerDefiningResourceId;

  private BaseLayerDefinition() {
  }

  public static BaseLayerDefinition withBaseElements(final BaseElementDefinition... baseElements) {
    return BaseLayerDefinition.withBaseElements(Arrays.asList(baseElements));
  }

  public static BaseLayerDefinition withBaseElements(final List<BaseElementDefinition> baseElements) {
    final BaseLayerDefinition baseLayerDefinition = new BaseLayerDefinition();
    baseLayerDefinition.setBaseElementDefinitions(baseElements);
    return baseLayerDefinition;
  }

  public void setBaseElementDefinitions(final List<BaseElementDefinition> baseElements) {
    this.baseElements = baseElements;
  }

  public void addBaseElementDefinition(final BaseElementDefinition definition) {
    baseElements.add(definition);
  }

  @JsonProperty("baseElements")
  public List<BaseElementDefinition> getBaseElementDefinitions() {
    return baseElements;
  }

  @JsonIgnore
  public UUID getBaseLayerDefiningResourceId() {
    return baseLayerDefiningResourceId;
  }

  public void setBaseLayerDefiningResourceId(UUID baseLayerDefiningResourceId) {
    this.baseLayerDefiningResourceId = baseLayerDefiningResourceId;
  }

  public List<String> getSubresourceElements() {
    return subresourceElements;
  }

  public BaseLayerDefinition setSubresourceElements(List<String> subresourceElements) {
    this.subresourceElements = subresourceElements;
    return this;
  }

}
