package nl.knaw.huygens.alexandria.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("annotation")
@JsonInclude(Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnotationPojo extends AbstractAccountablePojo<AnnotationPojo> {
  private String type;
  private String value;
  private String locator;

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setLocator(String locator) {
    this.locator = locator;
  }

  public String getLocator() {
    return locator;
  }

}
