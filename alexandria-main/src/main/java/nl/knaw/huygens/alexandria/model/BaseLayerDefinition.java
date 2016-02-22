package nl.knaw.huygens.alexandria.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BaseLayerDefinition {
  List<BaseElementDefinition> baseElementDefinitions = new ArrayList<>();
  UUID baseLayerDefiningResourceId;

  private BaseLayerDefinition() {
  }

  public static BaseLayerDefinition withBaseElements(final BaseElementDefinition... baseElementDefinitions) {
    return BaseLayerDefinition.withBaseElements(Arrays.asList(baseElementDefinitions));
  }

  public static BaseLayerDefinition withBaseElements(final List<BaseElementDefinition> baseElementDefinitions) {
    final BaseLayerDefinition baseLayerDefinition = new BaseLayerDefinition();
    baseLayerDefinition.setBaseElementDefinitions(baseElementDefinitions);
    return baseLayerDefinition;
  }

  public void setBaseElementDefinitions(final List<BaseElementDefinition> baseElementDefinitions) {
    this.baseElementDefinitions = baseElementDefinitions;
  }

  public void addBaseElementDefinition(final BaseElementDefinition definition) {
    baseElementDefinitions.add(definition);
  }

  public List<BaseElementDefinition> getBaseElementDefinitions() {
    return baseElementDefinitions;
  }

  @JsonIgnore
  public UUID getBaseLayerDefiningResourceId() {
    return baseLayerDefiningResourceId;
  }

  public void setBaseLayerDefiningResourceId(UUID baseLayerDefiningResourceId) {
    this.baseLayerDefiningResourceId = baseLayerDefiningResourceId;
  }

  public static class BaseElementDefinition {
    String name = "";
    List<String> baseAttributes = new ArrayList<>();

    private BaseElementDefinition() {
    }

    public static BaseElementDefinition withName(final String name) {
      BaseElementDefinition baseElementDefinition = new BaseElementDefinition();
      baseElementDefinition.setName(name);
      return baseElementDefinition;
    }

    public BaseElementDefinition withAttributes(final String... attributes) {
      setBaseAttributes(Arrays.asList(attributes));
      return this;
    }

    public void setName(final String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public void setBaseAttributes(final List<String> baseAttributes) {
      this.baseAttributes = baseAttributes;
    }

    public List<String> getBaseAttributes() {
      return baseAttributes;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, baseAttributes);
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof BaseElementDefinition //
          && Objects.equals(this.getName(), ((BaseElementDefinition) other).getName())//
          && Objects.equals(this.getBaseAttributes(), ((BaseElementDefinition) other).getBaseAttributes());
    }

  }

}
