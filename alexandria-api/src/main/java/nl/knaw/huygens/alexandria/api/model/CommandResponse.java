package nl.knaw.huygens.alexandria.api.model;

import java.util.List;

import com.google.common.collect.Lists;

public class CommandResponse {
  private List<String> errorLines = Lists.newArrayList();
  boolean parametersAreValid = false;

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

}
