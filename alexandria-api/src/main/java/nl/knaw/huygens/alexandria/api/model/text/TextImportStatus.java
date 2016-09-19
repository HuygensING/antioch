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
import nl.knaw.huygens.alexandria.api.model.ProcessStatus;
import nl.knaw.huygens.alexandria.api.model.PropertyPrefix;

@JsonTypeName(JsonTypeNames.TEXTIMPORTSTATUS)
@JsonInclude(Include.NON_NULL)
public class TextImportStatus extends ProcessStatus {

  private List<String> validationErrors = Lists.newArrayList();
  private URI textURI;


  public void setTextURI(URI textURI) {
    this.textURI = textURI;
  }

  @JsonProperty(PropertyPrefix.LINK + "xml")
  public URI getTextURI() {
    return textURI;
  }

  public List<String> getValidationErrors() {
    return validationErrors;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}
