package nl.knaw.huygens.alexandria.test;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;

public class AlexandriaTest {

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  protected String fixQuotes(String stringWithSingleQuotes) {
    return stringWithSingleQuotes.replace("'", "\"");
  }

}
