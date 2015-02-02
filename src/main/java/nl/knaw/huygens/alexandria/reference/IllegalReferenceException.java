package nl.knaw.huygens.alexandria.reference;

public class IllegalReferenceException extends Exception {
  public IllegalReferenceException(String illegalId) {
    super("Illegal reference id: " + illegalId);
  }
}
