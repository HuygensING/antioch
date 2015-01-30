package nl.knaw.huygens.alexandria;

public class ReferenceExistsException extends Throwable {
  public ReferenceExistsException(String id) {
    super("Reference already exists for id: " + id);
  }
}
