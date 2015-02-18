package nl.knaw.huygens.alexandria;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import nl.knaw.huygens.alexandria.reference.ReferenceStore;

public class InMemoryReferenceStore implements ReferenceStore {
  private final List<String> references = Lists.newArrayList();

  @Override
  public boolean createReference(final String id) {
    Preconditions.checkNotNull(id, "id must not be null");
    Preconditions.checkArgument(!id.isEmpty(), "id must not be empty");
    
    if (references.contains(id)) {
      return false;
    }

    references.add(id);
    return true;
  }
}
