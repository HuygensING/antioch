package nl.knaw.huygens.alexandria.api.model.text;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;

@JsonTypeName(JsonTypeNames.TEXTANNOTATIONINFO)
@JsonInclude(Include.NON_NULL)
public class TextRangeAnnotationInfo extends JsonWrapperObject {

  private String annotates;

  public TextRangeAnnotationInfo setAnnotates(String annotates) {
    this.annotates = annotates;
    return this;
  }

  public String getAnnotates() {
    return annotates;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}
