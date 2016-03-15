package nl.knaw.huygens.alexandria.test;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;

public abstract class AlexandriaTest {

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  protected String singleQuotesToDouble(String stringWithSingleQuotes) {
    return stringWithSingleQuotes.replace("'", "\"");
  }

}
