package nl.knaw.huygens.alexandria.text;

import java.util.Map;

public class Tag {

  private String name;

  public String getName() {
    return name;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  private Map<String, String> attributes;

  public Tag setName(String name) {
    this.name = name;
    return this;
  }

  public Tag setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  @Override
  public String toString() {
    return "Tag:<" + name + " " + attributes + ">";
  }

}
