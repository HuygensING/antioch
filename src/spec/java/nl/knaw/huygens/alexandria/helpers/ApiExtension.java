package nl.knaw.huygens.alexandria.helpers;

import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.internal.listener.AssertResultRenderer;

public class ApiExtension implements ConcordionExtension {
  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    System.err.println("*** API EXTENSION ***");

    IncludesJsonCommand includesJson = new IncludesJsonCommand();
    includesJson.addAssertEqualsListener(new AssertResultRenderer());
    concordionExtender.withCommand("https://alexandria.huygens.knaw.nl", "includesJson", includesJson);
  }
}
