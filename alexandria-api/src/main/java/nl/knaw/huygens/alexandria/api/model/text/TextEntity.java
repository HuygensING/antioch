package nl.knaw.huygens.alexandria.api.model.text;

import java.net.URI;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.Entity;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;
import nl.knaw.huygens.alexandria.api.model.text.view.TextViewEntity;

@JsonTypeName(JsonTypeNames.RESOURCETEXT)
@JsonInclude(Include.NON_NULL)
public class TextEntity extends JsonWrapperObject implements Entity {

  @JsonProperty("views")
  private List<TextViewEntity> textViews = Lists.newArrayList();

  @JsonProperty(PropertyPrefix.LINK + "xml")
  private URI xmlURI;

  @JsonProperty(PropertyPrefix.LINK + "dot")
  private URI dotURI;

  public TextEntity() {
  }

  public void setTextViews(List<TextViewEntity> textViews) {
    this.textViews = textViews;
  }

  public List<TextViewEntity> getTextViews() {
    return textViews;
  }

  public void setXmlURI(URI xmlURI) {
    this.xmlURI = xmlURI;
  }

  public URI getXmlURI() {
    return this.xmlURI;
  }

  public void setDotURI(URI dotURI) {
    this.dotURI = dotURI;
  }

  public URI getDotURI() {
    return this.dotURI;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}