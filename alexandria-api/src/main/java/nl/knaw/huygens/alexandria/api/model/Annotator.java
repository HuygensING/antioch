package nl.knaw.huygens.alexandria.api.model;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;

@JsonTypeName(JsonTypeNames.ANNOTATOR)
@JsonInclude(Include.NON_EMPTY)
public class Annotator extends JsonWrapperObject {

  private String code = "";
  private String description = "";

  @JsonProperty(PropertyPrefix.LINK + "resource")
  private URI resourceURI;

  public Annotator() {
  }

  public Annotator setCode(String code) {
    this.code = code;
    return this;
  }

  public String getCode() {
    return code;
  }

  public Annotator setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Annotator setResourceURI(URI resourceURI) {
    this.resourceURI = resourceURI;
    return this;
  }

  public URI getResourceURI() {
    return resourceURI;
  }

}
