package nl.knaw.huygens.alexandria.service;

import java.util.UUID;

import nl.knaw.huygens.alexandria.reference.IllegalReferenceException;
import nl.knaw.huygens.alexandria.reference.ReferenceExistsException;
import nl.knaw.huygens.alexandria.reference.ReferenceStore;

public class ReferenceService {
  private final ReferenceStore referenceStore;

  public ReferenceService(final ReferenceStore referenceStore) {
    this.referenceStore = referenceStore;
  }

  public UUID createReference(final String ref) {
    final UUID uuid = UUID.randomUUID();
    referenceStore.createReference(uuid, ref);
    return uuid;
  }

  public void createReference(final UUID uuid, final String ref) throws ReferenceExistsException {
    if (!referenceStore.createReference(uuid, ref)) {
      throw new ReferenceExistsException(uuid.toString());
    }
  }

  public void createReference(final String id, final String ref)
      throws IllegalReferenceException, ReferenceExistsException {
    try {
      createReference(UUID.fromString(id), ref);
    } catch (IllegalArgumentException dulyNoted) {
      throw new IllegalReferenceException(id);
    }
  }
}
