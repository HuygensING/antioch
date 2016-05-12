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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import nl.knaw.huygens.alexandria.api.model.search.QueryField;

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
