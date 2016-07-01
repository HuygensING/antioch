package nl.knaw.huygens.alexandria.query;

/*
 * #%L
 * alexandria-service
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

import nl.knaw.huygens.alexandria.api.model.search.QueryField;
import nl.knaw.huygens.alexandria.api.model.search.QueryFunction;

class WhereToken {
  private final QueryField property;
  private final QueryFunction function;
  private final List<Object> parameters;

  public WhereToken(QueryField property, QueryFunction function, List<Object> parameters) {
    this.property = property;
    this.function = function;
    this.parameters = parameters;
  }

  public QueryField getProperty() {
    return property;
  }

  public QueryFunction getFunction() {
    return function;
  }

  public List<Object> getParameters() {
    return parameters;
  }

  public boolean hasResourceProperty() {
    return QueryField.RESOURCE_FIELDS.contains(getProperty());
  }

}
