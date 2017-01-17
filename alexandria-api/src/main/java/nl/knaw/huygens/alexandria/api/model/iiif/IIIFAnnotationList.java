package nl.knaw.huygens.alexandria.api.model.iiif;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Maps;

import nl.knaw.huygens.alexandria.api.model.w3c.WebAnnotationPrototype;

@JsonPropertyOrder({ "@context", "resources" })
public class IIIFAnnotationList {

  private List<WebAnnotationPrototype> resources = new ArrayList<>();
  @JsonProperty("@context")
  private String context = "";

  // variable part
  private Map<String, Object> otherProperties = Maps.newHashMap();


  public void setContext(String context) {
    this.context = context;
  }

  public String getContext() {
    return context;
  }

  public void setResources(List<WebAnnotationPrototype> resources) {
    this.resources = resources;
  }

  public List<WebAnnotationPrototype> getResources() {
    return resources;
  }

  @JsonIgnore
  public void setOtherProperties(Map<String, Object> otherProperties) {
    this.otherProperties = otherProperties;
  }

  @JsonAnySetter
  public void putKeyValue(String key, Object value) {
    getOtherProperties().put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getOtherProperties() {
    return otherProperties;
  }
}
