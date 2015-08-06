package nl.knaw.huygens.alexandria.query;

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
}
