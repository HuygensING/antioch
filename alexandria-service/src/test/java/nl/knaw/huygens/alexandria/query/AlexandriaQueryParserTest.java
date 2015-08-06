package nl.knaw.huygens.alexandria.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.endpoint.search.AlexandriaQuery;

public class AlexandriaQueryParserTest {
  @Test
  public void testUnkownFindValueThrowsException() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFind("foobar");
    try {
      AlexandriaQueryParser.parse(aQuery);
      fail("AlexandriaQueryParseException expected");

    } catch (AlexandriaQueryParseException e) {
      Log.info("error message: {}", e.getMessage());
      assertThat(e.getMessage()).contains("foobar");
    }
  }

  @Test
  public void testReturnFields() {
    AlexandriaQuery aQuery = new AlexandriaQuery();
    aQuery.setFind("annotation");
    aQuery.setFields("id, resource.id, subresource.id");
    ParsedAlexandriaQuery paq = AlexandriaQueryParser.parse(aQuery);
    assertThat(paq.getReturnFields()).containsExactly("id", "resource.id", "subresource.id");
  }
}
