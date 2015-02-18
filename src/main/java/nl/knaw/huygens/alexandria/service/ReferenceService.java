package nl.knaw.huygens.alexandria.service;

import com.google.common.base.Strings;
import nl.knaw.huygens.alexandria.reference.IllegalReferenceException;
import nl.knaw.huygens.alexandria.reference.ReferenceExistsException;
import nl.knaw.huygens.alexandria.reference.ReferenceStore;

public class ReferenceService {
  private final ReferenceStore referenceStore;

  public ReferenceService(final ReferenceStore referenceStore) {
    this.referenceStore = referenceStore;
  }

  public void createReference(final String id) throws IllegalReferenceException, ReferenceExistsException {
    if (Strings.isNullOrEmpty(id)) {
      throw new IllegalReferenceException(id);
    }

    if (!referenceStore.createReference(id)) {
      throw new ReferenceExistsException(id);
    }
  }
}
