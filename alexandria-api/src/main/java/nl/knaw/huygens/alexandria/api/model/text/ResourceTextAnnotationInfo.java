package nl.knaw.huygens.alexandria.api.model.text;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.JsonWrapperObject;

@JsonTypeName(JsonTypeNames.TEXTANNOTATIONINFO)
@JsonInclude(Include.NON_NULL)
public class ResourceTextAnnotationInfo extends JsonWrapperObject {

  private String annotates;

  public ResourceTextAnnotationInfo setAnnotates(String annotates) {
    this.annotates = annotates;
    return this;
  }

  public String getAnnotates() {
    return annotates;
  }

}
