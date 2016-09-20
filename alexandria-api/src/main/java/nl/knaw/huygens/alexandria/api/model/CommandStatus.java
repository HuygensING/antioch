package nl.knaw.huygens.alexandria.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;

@JsonTypeName(JsonTypeNames.COMMANDSTATUS)
@JsonInclude(Include.NON_NULL)
public class CommandStatus extends ProcessStatus {

  private Object result;

  public void setResult(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }

}
