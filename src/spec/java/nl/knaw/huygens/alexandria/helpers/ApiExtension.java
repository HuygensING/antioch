package nl.knaw.huygens.alexandria.helpers;

import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.internal.listener.AssertResultRenderer;

public class ApiExtension implements ConcordionExtension {
  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    registerIncludesJsonCommand(concordionExtender);
    registerLinkedCSS(concordionExtender);
  }

  private void registerLinkedCSS(ConcordionExtender concordionExtender) {
    concordionExtender.withLinkedCSS("/concordion.css", new Resource("/concordion.css"));
  }

  private void registerIncludesJsonCommand(ConcordionExtender concordionExtender) {
    IncludesJsonCommand includesJson = new IncludesJsonCommand();
    includesJson.addAssertEqualsListener(new AssertResultRenderer());
    concordionExtender.withCommand("https://alexandria.huygens.knaw.nl", "includesJson", includesJson);
  }
}
