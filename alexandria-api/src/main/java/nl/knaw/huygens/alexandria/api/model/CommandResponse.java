package nl.knaw.huygens.alexandria.api.model;

/*
 * #%L
 * alexandria-api
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.Lists;

public class CommandResponse {
  private List<String> errorLines = Lists.newArrayList();
  boolean parametersAreValid = false;
  private Object result;
  private boolean async = false;
  private UUID statusId;

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

  public void setASync(boolean async) {
    this.async = async;
  }

  public boolean isASync() {
    return async;
  }

  public void setStatusId(UUID statusId) {
    this.statusId = statusId;
  }

  public UUID getStatusId() {
    return statusId;
  }

}
