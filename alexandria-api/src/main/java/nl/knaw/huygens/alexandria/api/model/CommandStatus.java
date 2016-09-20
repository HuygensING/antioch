package nl.knaw.huygens.alexandria.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;

@JsonTypeName(JsonTypeNames.COMMANDSTATUS)
@JsonInclude(Include.NON_NULL)
public class CommandStatus extends ProcessStatus {

  private Object result;
  private boolean success = false;
  private String errorMessage;

  public void setResult(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public boolean getSuccess() {
    return success;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

}
