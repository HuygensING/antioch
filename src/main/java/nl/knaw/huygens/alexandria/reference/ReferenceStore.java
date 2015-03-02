package nl.knaw.huygens.alexandria.reference;

import java.util.UUID;

public interface ReferenceStore {
  boolean createReference(UUID uuid,  String ref);
}
