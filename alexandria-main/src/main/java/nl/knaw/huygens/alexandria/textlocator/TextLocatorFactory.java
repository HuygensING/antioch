package nl.knaw.huygens.alexandria.textlocator;

public class TextLocatorFactory {

  public static AlexandriaTextLocator fromString(String locatorString) throws TextLocatorParseException {
    String[] parts = locatorString.split(":", 2);
    String prefix = parts[0];
    if (ByIdTextLocator.PREFIX.equals(prefix)) {
      return new ByIdTextLocator().withId(parts[1]);
    }
    throw new TextLocatorParseException("locator prefix '" + prefix + "' not recognized");
  }
}
