package nl.knaw.huygens.alexandria.external;

import java.util.UUID;

public interface ResourceStore {
  boolean createResource(UUID uuid, String res);

  String getReference(UUID uuid);
}
