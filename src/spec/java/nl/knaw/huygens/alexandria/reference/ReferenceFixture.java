package nl.knaw.huygens.alexandria.reference;

import nl.knaw.huygens.alexandria.InMemoryReferenceStore;
import nl.knaw.huygens.alexandria.service.ReferenceService;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class ReferenceFixture {
  private final ReferenceService service;

  public ReferenceFixture() {
    this.service = new ReferenceService(new InMemoryReferenceStore());
  }

  public String createReference(final String id) {
    try {
      service.createReference(id);
      return "201 Created";
    } catch (IllegalReferenceException e) {
      return "400 Bad Request";
    } catch (ReferenceExistsException e) {
      return "409 Conflict";
    }
  }
}
