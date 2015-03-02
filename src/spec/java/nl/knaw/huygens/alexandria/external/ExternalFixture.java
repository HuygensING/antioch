package nl.knaw.huygens.alexandria.external;

import nl.knaw.huygens.alexandria.InMemoryReferenceStore;
import nl.knaw.huygens.alexandria.service.ReferenceService;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class ExternalFixture {
  private final ReferenceService service;

  public ExternalFixture() {
    this.service = new ReferenceService(new InMemoryReferenceStore());
  }

  public String createReference(final String id) {
    service.createReference(id);
    return "201 Created";
  }
}
