package nl.knaw.huygens.alexandria.exception;

public class MissingEntityException extends BadRequestException {
  private static final long serialVersionUID = 1L;

  public MissingEntityException() {
    super("Missing entity (empty request body)");
  }
}
