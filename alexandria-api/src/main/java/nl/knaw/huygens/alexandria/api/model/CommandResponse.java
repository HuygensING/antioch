package nl.knaw.huygens.alexandria.api.model;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.Lists;

public class CommandResponse {
  private List<String> errorLines = Lists.newArrayList();
  boolean parametersAreValid = false;
  private Object result;

  public CommandResponse addErrorLine(String errorLine) {
    errorLines.add(errorLine);
    return this;
  }

  public Boolean success() {
    return errorLines.isEmpty();
  }

  public void setParametersAreValid(boolean b) {
    parametersAreValid = b;
  }

  public boolean parametersAreValid() {
    return parametersAreValid;
  }

  public List<String> getErrorLines() {
    return errorLines;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

  public void setResult(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }

}
