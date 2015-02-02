package nl.knaw.huygens.alexandria.reference;

public interface ReferenceStore {
  void createReference(String id) throws IllegalReferenceException, ReferenceExistsException;
}
