package nl.knaw.huygens.alexandria.endpoint.webannotation;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Maps;

@JsonPropertyOrder({ "@context", "id", "created", "modified" })
public class WebAnnotationPrototype {
  // for this phase, only accept the json-ld mirador uses.

  // required/fixed part
  private String id;
  private String created;
  private String modified;

  // variable part
  private Map<String, Object> variablePart = Maps.newHashMap();

  public WebAnnotationPrototype setId(String id) {
    this.id = id;
    return this;
  }

  public String getId() {
    return id;
  }

  public WebAnnotationPrototype setCreated(String created) {
    this.created = created;
    if (this.getModified() == null) {
      this.setModified(this.created);
    }
    return this;
  }

  public String getCreated() {
    return created;
  }

  public void setModified(String modified) {
    this.modified = modified;
  }

  public String getModified() {
    return modified;
  }

  @JsonAnySetter
  public void putKeyValue(String key, Object value) {
    variablePart.put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getVariablePart() {
    return variablePart;
  }

  public void setVariablePart(Map<String, Object> variablePart) {
    this.variablePart = variablePart;
  }

}
