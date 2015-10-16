package nl.knaw.huygens.alexandria.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import nl.knaw.huygens.alexandria.model.search.QueryField;

@RunWith(Parameterized.class)
public class QueryFieldGettersTest {
  @Parameters
  public static QueryField[] data() {
    return QueryField.values();
  }

  @Parameter(0)
  public QueryField queryField;

  @Test
  public void testEveryQueryFieldHasAGetter() {
    assertThat(QueryFieldGetters.get(queryField))//
        .overridingErrorMessage("No getter for QueryField '%s' defined in QueryFieldGetters", queryField)//
        .isNotNull();
  }
}
