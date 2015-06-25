package nl.knaw.huygens.alexandria.model;

public class AccountablePointer<T extends Accountable> {
  Class<T> clazz;
  String identifier;

  public AccountablePointer(Class<T> clazz, String identifier) {
    this.clazz = clazz;
    this.identifier = identifier;
  }

  public Class<T> getAccountableClass() {
    return clazz;
  }

  public String getIdentifier() {
    return identifier;
  }

}
