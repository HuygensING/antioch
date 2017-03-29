package nl.knaw.huygens.alexandria.exception;

public class IllegalOverlapException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public IllegalOverlapException(String message) {
    super(message);
  }
}
