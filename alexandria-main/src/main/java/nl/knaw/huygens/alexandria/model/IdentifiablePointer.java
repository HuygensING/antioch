package nl.knaw.huygens.alexandria.model;

public class IdentifiablePointer<T extends Identifiable> {
  Class<T> clazz;
  String identifier;

  public IdentifiablePointer(Class<T> clazz, String identifier) {
    this.clazz = clazz;
    this.identifier = identifier;
  }

  public Class<T> getIdentifiableClass() {
    return clazz;
  }

  public String getIdentifier() {
    return identifier;
  }

}
