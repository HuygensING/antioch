package nl.knaw.huygens.alexandria.textlocator;

public class ByIdTextLocator implements AlexandriaTextLocator {
  static final String PREFIX = "id";
  String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public AlexandriaTextLocator withId(String id) {
    setId(id);
    return this;
  }

  @Override
  public String toString() {
    return PREFIX + ":" + id;
  }
}
