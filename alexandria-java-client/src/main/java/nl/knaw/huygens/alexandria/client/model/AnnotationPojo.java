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

  public AnnotationPojo setType(String type) {
    this.type = type;
    return this;
  }

  public String getType() {
    return type;
  }

  public AnnotationPojo setValue(String value) {
    this.value = value;
    return this;
  }

  public String getValue() {
    return value;
  }

  public AnnotationPojo setLocator(String locator) {
    this.locator = locator;
    return this;
  }

  public String getLocator() {
    return locator;
  }

}
