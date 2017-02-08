package nl.knaw.huygens.alexandria.api.model.text.view;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.Entity;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;

@JsonTypeName(JsonTypeNames.TEXTVIEW)
@JsonInclude(Include.NON_NULL)
public class TextViewEntity extends JsonWrapperObject implements Entity {
  public String id;

  @JsonProperty(PropertyPrefix.LINK + "xml")
  private URI xmlURI;

  @JsonProperty(PropertyPrefix.LINK + "definition")
  private URI definitionURI;

  public TextViewEntity() {
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setXmlURI(URI xmlURI) {
    this.xmlURI = xmlURI;
  }

  public URI getXmlURI() {
    return this.xmlURI;
  }

  public void setDefinitionURI(URI definitionURI) {
    this.definitionURI = definitionURI;
  }

  public URI getDefinitionURI() {
    return this.definitionURI;
  }

}
