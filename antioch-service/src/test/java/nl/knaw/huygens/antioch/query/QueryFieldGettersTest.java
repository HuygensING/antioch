package nl.knaw.huygens.antioch.query;

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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import nl.knaw.huygens.antioch.api.model.search.QueryField;

@RunWith(Parameterized.class)
public class QueryFieldGettersTest {
  @Parameters
  public static QueryField[] data() {
    return QueryField.values();
  }

  @Parameter
  public QueryField queryField;

  @Test
  public void testEveryQueryFieldHasAGetter() {
    assertThat(QueryFieldGetters.get(queryField))//
        .overridingErrorMessage("No getter for QueryField '%s' defined in QueryFieldGetters", queryField)//
        .isNotNull();
  }
}
