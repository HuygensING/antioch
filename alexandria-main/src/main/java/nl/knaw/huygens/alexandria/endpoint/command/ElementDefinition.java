package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.Map;

public class ElementDefinition {
  String name;
  Map<String, String> attributes;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }
}
