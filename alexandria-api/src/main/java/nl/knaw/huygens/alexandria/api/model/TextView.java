package nl.knaw.huygens.alexandria.api.model;

import java.util.HashMap;
import java.util.Map;

public class TextView {
  private String description = "";
  private Map<String, ElementView> elementViewMap = new HashMap<>();

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

}
