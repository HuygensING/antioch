package nl.knaw.huygens.alexandria.client;

public class AlexandriaException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public AlexandriaException() {
    super();
  }

  public AlexandriaException(String message) {
    super(message);
  }

}
