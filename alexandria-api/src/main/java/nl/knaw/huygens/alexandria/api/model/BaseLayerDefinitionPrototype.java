package nl.knaw.huygens.alexandria.api.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("baseLayerDefinition")
public class BaseLayerDefinitionPrototype extends JsonWrapperObject implements Prototype {
  private List<BaseElementDefinition> baseElements = new ArrayList<>();
  private List<String> subresourceElements = new ArrayList<>();

  public BaseLayerDefinitionPrototype setBaseElements(BaseElementDefinition... baseElements) {
    this.baseElements.addAll(Arrays.asList(baseElements));
    return this;
  }

  public List<BaseElementDefinition> getBaseElements() {
    return baseElements;
  }

  public BaseLayerDefinitionPrototype setSubresourceElements(String... subresourceElements) {
    this.subresourceElements.addAll(Arrays.asList(subresourceElements));
    return this;
  }

  public List<String> getSubresourceElements() {
    return subresourceElements;
  }

}
