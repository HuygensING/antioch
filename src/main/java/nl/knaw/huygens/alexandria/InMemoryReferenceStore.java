package nl.knaw.huygens.alexandria;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import nl.knaw.huygens.alexandria.reference.IllegalReferenceException;
import nl.knaw.huygens.alexandria.reference.ReferenceExistsException;
import nl.knaw.huygens.alexandria.reference.ReferenceStore;

public class InMemoryReferenceStore implements ReferenceStore {
  private final List<String> references = Lists.newArrayList();

  @Override
  public void createReference(final String id) throws IllegalReferenceException, ReferenceExistsException {
    if (Strings.isNullOrEmpty(id)) {
      throw new IllegalReferenceException(id);
    }

    if (references.contains(id)) {
      throw new ReferenceExistsException(id);
    }

    references.add(id);
  }
}
