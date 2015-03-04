package nl.knaw.huygens.alexandria.external;

import java.util.UUID;

public interface ReferenceStore {
  boolean createReference(UUID uuid,  String ref);

  String getReference(UUID uuid);
}
