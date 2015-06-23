package nl.knaw.huygens.alexandria.model;

public class AccountablePointer {
  Class<? extends Accountable> clazz;
  String identifier;

  public AccountablePointer(Class<? extends Accountable> clazz, String identifier) {
    this.clazz = clazz;
    this.identifier = identifier;
  }

  public Class<? extends Accountable> getAccountableClass() {
    return clazz;
  }

  public String getIdentifier() {
    return identifier;
  }

}
