package nl.knaw.huygens.alexandria.api.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BaseElementDefinition {
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
