package nl.knaw.huygens.alexandria.api.model;

/*
 * #%L
 * alexandria-api
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
