package nl.knaw.huygens.alexandria.query;

/*
 * #%L
 * alexandria-service
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
