package nl.knaw.huygens.alexandria.service;

import nl.knaw.huygens.alexandria.reference.IllegalReferenceException;
import nl.knaw.huygens.alexandria.reference.ReferenceExistsException;
import nl.knaw.huygens.alexandria.reference.ReferenceStore;

public class ReferenceService {
  private final ReferenceStore referenceStore;

  public ReferenceService(final ReferenceStore referenceStore) {
    this.referenceStore = referenceStore;
  }

  public String createReference(final String id) {
    try {
      referenceStore.createReference(id);
      return "201 Created";
    } catch (IllegalReferenceException e) {
      return "400 Bad Request";
    } catch (ReferenceExistsException e) {
      return "409 Conflict";
    }
  }
}
