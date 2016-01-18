package nl.knaw.huygens.alexandria.endpoint.resource.text;

/*
 * #%L
 * alexandria-main
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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import nl.knaw.huygens.alexandria.endpoint.Entity;
import nl.knaw.huygens.alexandria.endpoint.JsonWrapperObject;

@JsonTypeName("textQueryResult")
public class TextQueryResult extends JsonWrapperObject implements Entity {
  private List<String> errors = Lists.newArrayList();
  private List<String> results = Lists.newArrayList();

  public void addError(String error) {
    errors.add(error);
  }

  public List<String> getErrors() {
    return errors;
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public void addResult(String result) {
    results.add(result);
  }

  public List<String> getResults() {
    return results;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())//
        .add("errors", errors)//
        .add("results", results)//
        .toString();
  }
}
