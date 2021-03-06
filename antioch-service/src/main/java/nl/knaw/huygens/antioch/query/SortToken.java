package nl.knaw.huygens.antioch.query;

import nl.knaw.huygens.antioch.api.model.search.QueryField;

/*
 * #%L
 * antioch-service
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

class SortToken {
  private QueryField field;
  private boolean ascending = true;

  public QueryField getField() {
    return field;
  }

  public SortToken setField(final QueryField field) {
    this.field = field;
    return this;
  }

  public boolean isAscending() {
    return ascending;
  }

  public SortToken setAscending(final boolean ascending) {
    this.ascending = ascending;
    return this;
  }
}
