package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.List;

import com.google.common.collect.Lists;

public class CommandResponse {
  private List<String> errorLines = Lists.newArrayList();
  boolean paremetersAreValid = false;

  public CommandResponse addErrorLine(String errorLine) {
    errorLines.add(errorLine);
    return this;
  }

  public Boolean success() {
    return errorLines.isEmpty();
  }

  public void setParametersAreValid() {
    paremetersAreValid = true;
  }

  public boolean paremetersAreValid() {
    return paremetersAreValid;
  }

  public List<String> getErrorLines() {
    return errorLines;
  }

}
