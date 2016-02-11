package nl.knaw.huygens.alexandria.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.knaw.huygens.alexandria.model.BaseLayerDefinition.BaseElementDefinition;

public class BaseLayerDefinition {
  List<BaseElementDefinition> baseElementDefinitions = new ArrayList<>();

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


  }

}
