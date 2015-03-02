package nl.knaw.huygens.alexandria.external;

public class ReferenceExistsException extends Exception {
  public ReferenceExistsException(String id) {
    super("Reference already exists for id: " + id);
  }
}
