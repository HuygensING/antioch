package nl.knaw.huygens.alexandria.client;

/*
 * #%L
 * alexandria-java-client
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
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

import java.util.Optional;

import javax.ws.rs.core.Response;

public class RestResult<T> {
  private boolean failure = false;
  private T cargo;
  private Response response;
  private Exception exception;
  private String errorMessage;

  public static <T extends Object> RestResult<T> failingResult(Response response) {
    RestResult<T> result = new RestResult<>();
    result.setFail(true);
    result.setResponse(response);
    return result;
  }

  public static <T extends Object> RestResult<T> failingResult(Exception exception) {
    RestResult<T> result = new RestResult<>();
    result.setFail(true);
    result.setException(exception);
    return result;
  }

  public static <T extends Object> RestResult<T> failingResult(String errorMessage) {
    RestResult<T> result = new RestResult<>();
    result.setFail(true);
    result.setErrorMessage(errorMessage);
    return result;
  }

  public void setCargo(T cargo) {
    this.cargo = cargo;
  }

  public T get() {
    return cargo;
  }

  public void setFail(boolean failure) {
    this.failure = failure;
  }

  public boolean hasFailed() {
    return failure;
  }

  private void setResponse(Response response) {
    this.response = response;
  }

  public Optional<Response> getResponse() {
    return Optional.ofNullable(response);
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }

  public Optional<Exception> getException() {
    return Optional.ofNullable(exception);
  }

  private void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Optional<String> getErrorMessage() {
    return Optional.ofNullable(errorMessage);
  }

  public Optional<String> getFailureCause() {
    String cause = null;
    if (response != null) {
      cause = "Unexpected return status: " + response.getStatus() + " " + response.getStatusInfo().toString();

    } else if (errorMessage != null) {
      cause = errorMessage;

    } else if (exception != null) {
      cause = exception.getMessage();
    }
    return Optional.ofNullable(cause);
  }

}
