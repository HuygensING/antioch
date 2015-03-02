package nl.knaw.huygens.alexandria;

import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import nl.knaw.huygens.alexandria.reference.ReferenceStore;

public class InMemoryReferenceStore implements ReferenceStore {
  private final Map<UUID, String> references = Maps.newConcurrentMap();

  @Override
  public boolean createReference(final UUID uuid, final String ref) {
    Preconditions.checkNotNull(uuid, "id must not be null");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(ref), "ref must not be null or empty");

    if (references.containsKey(uuid)) {
      return false;
    }

    references.put(uuid, ref);
    return true;
  }
}
