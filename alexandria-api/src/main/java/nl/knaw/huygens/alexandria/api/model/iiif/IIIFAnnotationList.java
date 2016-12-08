package nl.knaw.huygens.alexandria.api.model.iiif;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;

import nl.knaw.huygens.alexandria.api.model.w3c.WebAnnotationPrototype;

public class IIIFAnnotationList {

  // variable part
  private Map<String, Object> otherProperties = Maps.newHashMap();

  private List<WebAnnotationPrototype> resources = new ArrayList<>();

  @JsonAnySetter
  public void putKeyValue(String key, Object value) {
    getOtherProperties().put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getOtherProperties() {
    return otherProperties;
  }

  public List<WebAnnotationPrototype> getResources() {
    return resources;
  }

  public void setResources(List<WebAnnotationPrototype> resources) {
    this.resources = resources;
  }

  @JsonIgnore
  public void setOtherProperties(Map<String, Object> otherProperties) {
    this.otherProperties = otherProperties;
  }

}
